package com.platform.search.repository;

import com.platform.search.domain.entity.ArticleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文章搜索仓储
 */
@Repository
public interface ArticleSearchRepository extends ElasticsearchRepository<ArticleDocument, Long> {

    /**
     * 根据状态查询文章
     *
     * @param status 状态
     * @return 文章列表
     */
    List<ArticleDocument> findByStatus(Integer status);

    /**
     * 根据作者ID查询文章
     *
     * @param authorId 作者ID
     * @return 文章列表
     */
    List<ArticleDocument> findByAuthorId(Long authorId);

    /**
     * 根据分类ID查询文章
     *
     * @param categoryId 分类ID
     * @return 文章列表
     */
    List<ArticleDocument> findByCategoryId(Long categoryId);
}
