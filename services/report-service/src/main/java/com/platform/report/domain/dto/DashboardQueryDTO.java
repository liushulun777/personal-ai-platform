package com.platform.report.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大屏查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DashboardQueryDTO extends PageQuery {

    /**
     * 大屏名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;
}
