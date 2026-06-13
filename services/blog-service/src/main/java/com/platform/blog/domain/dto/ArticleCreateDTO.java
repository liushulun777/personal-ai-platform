package com.platform.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建文章DTO
 */
@Data
public class ArticleCreateDTO {

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    /**
     * 摘要
     */
    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    /**
     * 内容
     */
    private String content;

    /**
     * 封面图
     */
    @Size(max = 500, message = "封面图URL长度不能超过500个字符")
    private String cover;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否原创
     */
    private Integer isOriginal;

    /**
     * 来源URL
     */
    @Size(max = 500, message = "来源URL长度不能超过500个字符")
    private String sourceUrl;
}
