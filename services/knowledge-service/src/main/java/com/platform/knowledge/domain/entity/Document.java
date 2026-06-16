package com.platform.knowledge.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document")
public class Document extends BaseEntity {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文件类型: pdf, md, docx, txt
     */
    private String fileType;

    /**
     * 文件URL（MinIO）
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 提取的文本内容
     */
    private String content;

    /**
     * 分块数量
     */
    private Integer chunkCount;

    /**
     * 状态: 0-待处理, 1-处理中, 2-就绪, 3-失败
     */
    private Integer status;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 作者ID
     */
    private Long authorId;
}
