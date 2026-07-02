package com.platform.report.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度任务详情VO
 */
@Data
public class ScheduleDetailVO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 报表ID
     */
    private Long reportId;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * Cron表达式
     */
    private String cron;

    /**
     * 执行配置
     */
    private Object config;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 上次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextExecuteTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
