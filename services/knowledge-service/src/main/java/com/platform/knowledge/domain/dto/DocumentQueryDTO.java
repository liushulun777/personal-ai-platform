package com.platform.knowledge.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentQueryDTO extends PageQuery {

    /**
     * 文档标题（模糊查询）
     */
    private String title;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 分类ID
     */
    private Long categoryId;
}
