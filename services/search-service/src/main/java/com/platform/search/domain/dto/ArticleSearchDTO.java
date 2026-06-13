package com.platform.search.domain.dto;

import com.platform.common.core.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章搜索请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文章搜索请求")
public class ArticleSearchDTO extends PageQuery {

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词", example = "Spring Boot")
    private String keyword;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 作者ID
     */
    @Schema(description = "作者ID")
    private Long authorId;

    /**
     * 排序字段: relevance(相关度), time(时间), viewCount(浏览数), likeCount(点赞数)
     */
    @Schema(description = "排序字段", defaultValue = "relevance", allowableValues = {"relevance", "time", "viewCount", "likeCount"})
    private String sortBy = "relevance";

    /**
     * 排序方向: asc, desc
     */
    @Schema(description = "排序方向", defaultValue = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder = "desc";
}
