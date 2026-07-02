package com.platform.report.domain.dto;

import lombok.Data;

/**
 * 调度任务更新DTO
 */
@Data
public class ScheduleUpdateDTO {

    /**
     * 任务名称
     */
    private String name;

    /**
     * Cron表达式
     */
    private String cron;

    /**
     * 执行配置 (JSON)
     */
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
