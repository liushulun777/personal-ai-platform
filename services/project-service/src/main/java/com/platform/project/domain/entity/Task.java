package com.platform.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_task")
public class Task extends BaseEntity {

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 父任务ID（子任务支持）
     */
    private Long parentTaskId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 状态: 0-BACKLOG, 1-READY, 2-DOING, 3-REVIEW, 4-DONE, 5-BLOCKED
     */
    private Integer status;

    /**
     * 优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT
     */
    private Integer priority;

    /**
     * 来源: MANUAL, AI_GENERATED, AGENT_CREATED
     */
    private String sourceType;

    /**
     * 执行人ID
     */
    private Long assigneeId;

    /**
     * 报告人ID
     */
    private Long reporterId;

    /**
     * 截止日期
     */
    private LocalDate dueDate;

    /**
     * 阻塞原因
     */
    private String blockedReason;
}
