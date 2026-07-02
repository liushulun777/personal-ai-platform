package com.platform.report.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.report.typehandler.JsonbTypeHandler;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 调度任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_schedule", autoResultMap = true)
public class Schedule extends BaseEntity {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 报表ID
     */
    private Long reportId;

    /**
     * Cron表达式
     */
    private String cron;

    /**
     * 执行配置 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object config;

    /**
     * 状态: 0-停止 1-运行 2-暂停
     */
    private Integer status;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 备注
     */
    private String remark;
}
