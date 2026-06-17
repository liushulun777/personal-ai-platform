package com.platform.search.service;

import com.platform.search.domain.entity.ArticleDocument;

import java.util.List;

/**
 * 文章分块向量服务接口
 * 负责文章内容的分块、向量嵌入和存储
 */
public interface ArticleChunkService {

    /**
     * 处理文章：分块 + 向量嵌入 + 存储
     * 先删除旧分块，再创建新分块
     *
     * @param document 文章文档
     */
    void processArticle(ArticleDocument document);

    /**
     * 删除文章的所有分块
     *
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);

    /**
     * 向量语义搜索
     * 返回按相似度排序的文章ID列表及对应的最佳相似度分数
     *
     * @param queryText  查询文本
     * @param authorId   作者ID过滤（可选）
     * @param categoryId 分类ID过滤（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @param topK       返回数量
     * @return 文章ID列表（按相似度降序）
     */
    List<Long> semanticSearch(String queryText, Long authorId, Long categoryId,
                              java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, int topK);
}
