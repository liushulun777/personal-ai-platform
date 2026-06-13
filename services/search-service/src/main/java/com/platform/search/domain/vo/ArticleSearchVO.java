package com.platform.search.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章搜索结果VO
 */
@Data
@Schema(description = "文章搜索结果")
public class ArticleSearchVO {

    /**
     * 文章ID
     */
    @Schema(description = "文章ID")
    private Long id;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 摘要（高亮）
     */
    @Schema(description = "摘要")
    private String summary;

    /**
     * 内容片段（高亮）
     */
    @Schema(description = "内容片段")
    private String contentFragment;

    /**
     * 作者名称
     */
    @Schema(description = "作者名称")
    private String authorName;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 浏览数
     */
    @Schema(description = "浏览数")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    private Integer favoriteCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    private Integer commentCount;

    /**
     * 搜索得分
     */
    @Schema(description = "搜索得分")
    private Float score;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
