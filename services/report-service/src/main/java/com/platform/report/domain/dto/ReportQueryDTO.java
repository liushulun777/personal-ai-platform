package com.platform.report.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQueryDTO extends PageQuery {

    /**
     * 报表名称
     */
    private String name;

    /**
     * 报表类型
     */
    private String type;

    /**
     * 报表分类
     */
    private String category;

    /**
     * 状态
     */
    private Integer status;
}
