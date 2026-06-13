package com.platform.blog.domain.vo;

import lombok.Data;

/**
 * 博客统计VO
 */
@Data
public class BlogStatisticsVO {

    /**
     * 文章总数
     */
    private Long articleCount;

    /**
     * 分类数量
     */
    private Long categoryCount;

    /**
     * 标签数量
     */
    private Long tagCount;

    /**
     * 评论数量
     */
    private Long commentCount;

    /**
     * 总浏览量
     */
    private Long totalViews;

    /**
     * 总点赞数
     */
    private Long totalLikes;
}
