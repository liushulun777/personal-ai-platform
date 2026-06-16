package com.platform.knowledge.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文本分块实体（追加型表，不继承BaseEntity）
 */
@Data
@TableName("kb_chunk")
public class Chunk implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 分块内容
     */
    private String content;

    /**
     * Token数量
     */
    private Integer tokenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
