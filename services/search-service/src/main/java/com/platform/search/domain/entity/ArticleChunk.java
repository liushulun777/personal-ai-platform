package com.platform.search.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.search.typehandler.VectorTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章分块向量实体
 * 存储文章分块内容及对应的向量嵌入，支持 pgvector 语义搜索
 */
@Data
@TableName(value = "biz_article_chunk", autoResultMap = true)
public class ArticleChunk {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 分块序号
     */
    private Integer chunkIndex;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 向量嵌入 (pgvector)
     */
    @TableField(typeHandler = VectorTypeHandler.class)
    private float[] embedding;

    /**
     * 作者ID（冗余字段，用于多维筛选）
     */
    private Long authorId;

    /**
     * 分类ID（冗余字段，用于多维筛选）
     */
    private Long categoryId;

    /**
     * 标签列表JSON（冗余字段，用于多维筛选）
     */
    private String tags;

    /**
     * 发布时间（冗余字段，用于日期范围筛选）
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 查询时计算的相似度（非数据库字段）
     */
    @TableField(exist = false)
    private Double similarity;
}
