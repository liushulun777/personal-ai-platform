package com.platform.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.search.domain.entity.ArticleChunk;
import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.mapper.ArticleChunkMapper;
import com.platform.search.service.ArticleChunkService;
import com.platform.search.service.ChunkingService;
import com.platform.search.service.EmbeddingService;
import com.platform.search.typehandler.VectorTypeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章分块向量服务实现
 * 负责文章内容的分块、向量嵌入和存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleChunkServiceImpl implements ArticleChunkService {

    private final ArticleChunkMapper articleChunkMapper;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Value("${search.chunk.size:500}")
    private int chunkSize;

    @Value("${search.chunk.overlap:50}")
    private int chunkOverlap;

    @Value("${search.embedding.top-k:20}")
    private int defaultTopK;

    @Override
    public void processArticle(ArticleDocument document) {
        try {
            log.info("开始处理文章分块, articleId: {}", document.getId());

            // 1. 删除旧分块
            deleteByArticleId(document.getId());

            // 2. 拼接标题+摘要+内容作为完整文本
            String fullText = buildFullText(document);
            if (fullText == null || fullText.trim().isEmpty()) {
                log.warn("文章内容为空，跳过分块, articleId: {}", document.getId());
                return;
            }

            // 3. 分块
            List<String> chunks = chunkingService.chunk(fullText, chunkSize, chunkOverlap);
            if (chunks.isEmpty()) {
                log.warn("分块结果为空, articleId: {}", document.getId());
                return;
            }

            // 4. 批量生成向量嵌入
            List<float[]> embeddings = embeddingService.embedBatch(chunks);

            // 5. 构建冗余字段
            String tagsJson = buildTagsJson(document.getTags());

            // 6. 保存分块
            for (int i = 0; i < chunks.size(); i++) {
                ArticleChunk chunk = new ArticleChunk();
                chunk.setArticleId(document.getId());
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunk.setEmbedding(embeddings.get(i));
                chunk.setAuthorId(document.getAuthorId());
                chunk.setCategoryId(document.getCategoryId());
                chunk.setTags(tagsJson);
                chunk.setPublishTime(document.getPublishTime());
                chunk.setCreateTime(LocalDateTime.now());
                articleChunkMapper.insert(chunk);
            }

            log.info("文章分块处理完成, articleId: {}, chunkCount: {}", document.getId(), chunks.size());
        } catch (Exception e) {
            log.error("处理文章分块失败, articleId: {}", document.getId(), e);
            throw new RuntimeException("处理文章分块失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByArticleId(Long articleId) {
        int deleted = articleChunkMapper.delete(
                new LambdaQueryWrapper<ArticleChunk>()
                        .eq(ArticleChunk::getArticleId, articleId)
        );
        log.info("删除文章分块, articleId: {}, deleted: {}", articleId, deleted);
    }

    @Override
    public List<Long> semanticSearch(String queryText, Long authorId, Long categoryId,
                                     LocalDateTime startDate, LocalDateTime endDate, int topK) {
        try {
            // 1. 将查询文本转换为向量
            float[] queryEmbedding = embeddingService.embed(queryText);
            String embeddingStr = VectorTypeHandler.toVectorString(queryEmbedding);

            // 2. pgvector 语义搜索
            List<ArticleChunk> similarChunks = articleChunkMapper.searchSimilar(
                    embeddingStr, authorId, categoryId, startDate, endDate, topK);

            // 3. 按文章ID聚合，取每个文章的最高相似度
            Map<Long, Double> articleScoreMap = new LinkedHashMap<>();
            for (ArticleChunk chunk : similarChunks) {
                articleScoreMap.merge(chunk.getArticleId(), chunk.getSimilarity(), Math::max);
            }

            // 4. 按相似度降序返回文章ID列表
            List<Long> articleIds = new ArrayList<>(articleScoreMap.keySet());
            articleIds.sort((a, b) -> Double.compare(articleScoreMap.get(b), articleScoreMap.get(a)));

            return articleIds;
        } catch (Exception e) {
            log.error("语义搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建完整文本（标题 + 摘要 + 内容）
     */
    private String buildFullText(ArticleDocument document) {
        StringBuilder sb = new StringBuilder();
        if (document.getTitle() != null && !document.getTitle().isEmpty()) {
            sb.append(document.getTitle()).append("\n\n");
        }
        if (document.getSummary() != null && !document.getSummary().isEmpty()) {
            sb.append(document.getSummary()).append("\n\n");
        }
        if (document.getContent() != null && !document.getContent().isEmpty()) {
            sb.append(document.getContent());
        }
        return sb.toString().trim();
    }

    /**
     * 将标签列表转换为JSON字符串
     */
    private String buildTagsJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            log.warn("标签序列化失败", e);
            return "[]";
        }
    }
}
