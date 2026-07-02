package com.platform.report.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调度任务查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleQueryDTO extends PageQuery {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 报表ID
     */
    private Long reportId;

    /**
     * 状态
     */
    private Integer status;
}
