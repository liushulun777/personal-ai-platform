package com.platform.knowledge.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.knowledge.typehandler.VectorTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库向量嵌入实体（追加型表，不继承BaseEntity）
 */
@Data
@TableName("kb_embedding")
public class Embedding implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分块ID
     */
    private Long chunkId;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 向量嵌入（通过 VectorTypeHandler 映射 pgvector）
     */
    @TableField(typeHandler = VectorTypeHandler.class)
    private float[] embedding;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 相似度（来自查询计算，非数据库字段）
     */
    @TableField(exist = false)
    private Double similarity;
}
