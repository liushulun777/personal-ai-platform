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
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 状态: 0-待办, 1-进行中, 2-已完成, 3-已关闭
     */
    private Integer status;

    /**
     * 优先级: 0-低, 1-中, 2-高, 3-紧急
     */
    private Integer priority;

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
}
