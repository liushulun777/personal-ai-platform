package com.platform.search.service;

import com.platform.common.core.result.PageResult;
import com.platform.search.domain.dto.ArticleSearchDTO;
import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.domain.vo.ArticleSearchVO;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 搜索文章
     *
     * @param searchDTO 搜索条件
     * @return 搜索结果
     */
    PageResult<ArticleSearchVO> searchArticles(ArticleSearchDTO searchDTO);

    /**
     * 索引文章
     *
     * @param document 文章文档
     */
    void indexArticle(ArticleDocument document);

    /**
     * 批量索引文章
     *
     * @param documents 文章文档列表
     */
    void indexArticles(List<ArticleDocument> documents);

    /**
     * 删除文章索引
     *
     * @param articleId 文章ID
     */
    void deleteArticle(Long articleId);

    /**
     * 更新文章索引
     *
     * @param document 文章文档
     */
    void updateArticle(ArticleDocument document);

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词
     * @return 建议列表
     */
    List<String> getSuggestions(String keyword);
}
