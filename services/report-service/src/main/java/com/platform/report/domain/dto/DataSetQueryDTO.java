package com.platform.report.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据集查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSetQueryDTO extends PageQuery {

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集类型
     */
    private String type;

    /**
     * 数据源ID
     */
    private Long sourceId;

    /**
     * 状态
     */
    private Integer status;
}
