package com.platform.search.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章搜索文档
 */
@Data
@Document(indexName = "articles")
@Setting(shards = 1, replicas = 1)
public class ArticleDocument {

    /**
     * 文章ID
     */
    @Id
    private Long id;

    /**
     * 标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 摘要
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String summary;

    /**
     * 内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 作者ID
     */
    @Field(type = FieldType.Long)
    private Long authorId;

    /**
     * 作者名称
     */
    @Field(type = FieldType.Keyword)
    private String authorName;

    /**
     * 分类ID
     */
    @Field(type = FieldType.Long)
    private Long categoryId;

    /**
     * 分类名称
     */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    /**
     * 标签列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 状态: 0-草稿, 1-已发布, 2-已归档
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 浏览数
     */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Field(type = FieldType.Integer)
    private Integer likeCount;

    /**
     * 收藏数
     */
    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    /**
     * 评论数
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 是否置顶
     */
    @Field(type = FieldType.Integer)
    private Integer isTop;

    /**
     * 发布时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
}
