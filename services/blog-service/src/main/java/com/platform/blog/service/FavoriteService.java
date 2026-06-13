package com.platform.blog.service;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     */
    void favorite(Long articleId);

    /**
     * 取消收藏
     *
     * @param articleId 文章ID
     */
    void unfavorite(Long articleId);

    /**
     * 检查是否已收藏
     *
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long articleId);
}
