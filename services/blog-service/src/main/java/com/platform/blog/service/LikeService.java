package com.platform.blog.service;

/**
 * 点赞服务接口
 */
public interface LikeService {

    /**
     * 点赞
     *
     * @param targetId 目标ID
     * @param targetType 目标类型: 1-文章, 2-评论
     */
    void like(Long targetId, Integer targetType);

    /**
     * 取消点赞
     *
     * @param targetId 目标ID
     * @param targetType 目标类型: 1-文章, 2-评论
     */
    void unlike(Long targetId, Integer targetType);

    /**
     * 检查是否已点赞
     *
     * @param targetId 目标ID
     * @param targetType 目标类型: 1-文章, 2-评论
     * @return 是否已点赞
     */
    boolean isLiked(Long targetId, Integer targetType);
}
