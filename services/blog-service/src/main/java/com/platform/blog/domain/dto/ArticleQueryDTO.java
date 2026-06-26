package com.platform.blog.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQueryDTO extends PageQuery {

    /**
     * 标题（模糊查询）
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 状态: 0-草稿, 1-已发布, 2-已归档
     */
    private Integer status;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 是否应用数据权限过滤（管理页面传 true，公开页面不传或传 false）
     */
    private Boolean applyDataScope;
}
