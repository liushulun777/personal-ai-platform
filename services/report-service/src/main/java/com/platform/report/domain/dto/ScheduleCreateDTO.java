package com.platform.report.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调度任务创建DTO
 */
@Data
public class ScheduleCreateDTO {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String name;

    /**
     * 报表ID
     */
    @NotNull(message = "报表ID不能为空")
    private Long reportId;

    /**
     * Cron表达式
     */
    @NotBlank(message = "Cron表达式不能为空")
    private String cron;

    /**
     * 执行配置 (JSON)
     */
    @NotNull(message = "执行配置不能为空")
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
