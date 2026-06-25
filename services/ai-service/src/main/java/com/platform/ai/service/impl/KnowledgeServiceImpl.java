package com.platform.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.ai.service.AiService;
import com.platform.ai.domain.dto.KnowledgeQueryDTO;
import com.platform.ai.domain.entity.Chunk;
import com.platform.ai.domain.entity.Document;
import com.platform.ai.domain.entity.Embedding;
import com.platform.ai.domain.vo.KnowledgeAnswerVO;
import com.platform.ai.mapper.ChunkMapper;
import com.platform.ai.mapper.DocumentMapper;
import com.platform.ai.mapper.EmbeddingMapper;
import com.platform.ai.service.EmbeddingService;
import com.platform.ai.service.KnowledgeService;
import com.platform.ai.typehandler.VectorTypeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库RAG查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final EmbeddingService embeddingService;
    private final EmbeddingMapper embeddingMapper;
    private final ChunkMapper chunkMapper;
    private final DocumentMapper documentMapper;
    private final AiService aiService;

    @Value("${knowledge.embedding.dimension:1024}")
    private int embeddingDimension;

    @Override
    public KnowledgeAnswerVO query(KnowledgeQueryDTO queryDTO) {
        // 1. 将问题转换为嵌入向量
        float[] questionEmbedding = embeddingService.embed(queryDTO.getQuestion());

        // 2. 使用 pgvector 相似度搜索
        int topK = queryDTO.getTopK() != null ? queryDTO.getTopK() : 5;
        String embeddingStr = VectorTypeHandler.toVectorString(questionEmbedding);

        List<Embedding> similarEmbeddings = embeddingMapper.searchSimilar(
                embeddingStr, queryDTO.getCategoryId(), topK);

        if (similarEmbeddings.isEmpty()) {
            return KnowledgeAnswerVO.builder()
                    .answer("知识库中暂无相关内容，请先上传相关文档。")
                    .sources(List.of())
                    .build();
        }

        // 3. 获取关联的分块和文档信息
        List<Long> chunkIds = similarEmbeddings.stream()
                .map(Embedding::getChunkId)
                .collect(Collectors.toList());

        Map<Long, Chunk> chunkMap = chunkMapper.selectBatchIds(chunkIds).stream()
                .collect(Collectors.toMap(Chunk::getId, c -> c));

        List<Long> documentIds = similarEmbeddings.stream()
                .map(Embedding::getDocumentId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Document> documentMap = documentMapper.selectBatchIds(documentIds).stream()
                .collect(Collectors.toMap(Document::getId, d -> d));

        // 4. 构建上下文
        StringBuilder contextBuilder = new StringBuilder();
        List<KnowledgeAnswerVO.SourceInfo> sources = new ArrayList<>();

        for (Embedding emb : similarEmbeddings) {
            Chunk chunk = chunkMap.get(emb.getChunkId());
            Document doc = documentMap.get(emb.getDocumentId());

            if (chunk != null && doc != null) {
                contextBuilder.append(String.format("[%s]\n%s\n\n", doc.getTitle(), chunk.getContent()));

                sources.add(KnowledgeAnswerVO.SourceInfo.builder()
                        .documentId(doc.getId())
                        .documentTitle(doc.getTitle())
                        .chunkContent(chunk.getContent().length() > 200
                                ? chunk.getContent().substring(0, 200) + "..."
                                : chunk.getContent())
                        .similarity(emb.getSimilarity() != null ? emb.getSimilarity() : 0.0)
                        .build());
            }
        }

        // 5. 调用 AI 生成回答
        String systemPrompt = "你是一个知识库助手。请基于以下知识库内容回答用户的问题。" +
                "如果知识库中没有相关信息，请如实告知。\n\n" +
                "知识库内容：\n" + contextBuilder;

        String answer = aiService.chat(queryDTO.getQuestion(), systemPrompt);

        return KnowledgeAnswerVO.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }
}
