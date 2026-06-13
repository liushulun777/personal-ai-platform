package com.platform.blog.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailVO extends ArticleVO {

    /**
     * 内容
     */
    private String content;

    /**
     * 来源URL
     */
    private String sourceUrl;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;
}
