package com.platform.report.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceQueryDTO extends PageQuery {

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源类型
     */
    private String type;

    /**
     * 状态
     */
    private Integer status;
}
