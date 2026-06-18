package com.platform.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.ai.domain.entity.Chunk;
import com.platform.ai.domain.entity.Document;
import com.platform.ai.domain.entity.Embedding;
import com.platform.ai.mapper.ChunkMapper;
import com.platform.ai.mapper.DocumentMapper;
import com.platform.ai.mapper.EmbeddingMapper;
import com.platform.ai.service.ChunkingService;
import com.platform.ai.service.DocumentProcessingService;
import com.platform.ai.service.EmbeddingService;
import com.platform.ai.typehandler.VectorTypeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档处理服务实现（异步）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingServiceImpl implements DocumentProcessingService {

    private final DocumentMapper documentMapper;
    private final ChunkMapper chunkMapper;
    private final EmbeddingMapper embeddingMapper;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void processDocument(Long documentId, int chunkSize, int chunkOverlap) {
        log.info("开始处理文档: {}", documentId);

        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            log.error("文档不存在: {}", documentId);
            return;
        }

        try {
            // 更新状态为处理中
            document.setStatus(1);
            documentMapper.updateById(document);

            String content = document.getContent();
            if (content == null || content.isEmpty()) {
                log.warn("文档内容为空: {}", documentId);
                document.setStatus(3);
                documentMapper.updateById(document);
                return;
            }

            // 1. 文本分块
            List<String> chunks = chunkingService.chunk(content, chunkSize, chunkOverlap);
            log.info("文档 {} 分块完成，共 {} 个分块", documentId, chunks.size());

            // 2. 保存分块并生成嵌入
            for (int i = 0; i < chunks.size(); i++) {
                String chunkContent = chunks.get(i);

                // 保存分块
                Chunk chunk = new Chunk();
                chunk.setDocumentId(documentId);
                chunk.setChunkIndex(i);
                chunk.setContent(chunkContent);
                chunk.setTokenCount(chunkContent.length()); // 简化：用字符数代替token数
                chunk.setCreateTime(LocalDateTime.now());
                chunkMapper.insert(chunk);

                // 生成嵌入
                float[] embeddingVector = embeddingService.embed(chunkContent);

                // 保存嵌入
                Embedding embedding = new Embedding();
                embedding.setChunkId(chunk.getId());
                embedding.setDocumentId(documentId);
                embedding.setEmbedding(embeddingVector);
                embedding.setCreateTime(LocalDateTime.now());
                embeddingMapper.insert(embedding);

                if ((i + 1) % 10 == 0) {
                    log.info("文档 {} 已处理 {}/{} 个分块", documentId, i + 1, chunks.size());
                }
            }

            // 3. 更新文档状态
            document.setChunkCount(chunks.size());
            document.setStatus(2);
            documentMapper.updateById(document);

            log.info("文档 {} 处理完成，共 {} 个分块", documentId, chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: {}", documentId, e);
            document.setStatus(3);
            documentMapper.updateById(document);
        }
    }
}
