package com.platform.project.domain.event;

import lombok.Data;

/**
 * 任务创建事件
 */
@Data
public class TaskCreatedEvent {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 项目ID
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
     * 优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT
     */
    private Integer priority;

    /**
     * 来源: MANUAL, AI_GENERATED, AGENT_CREATED
     */
    private String sourceType;

    public TaskCreatedEvent() {
    }

    public TaskCreatedEvent(Long taskId, Long projectId, String title, String description,
                            Integer priority, String sourceType) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.sourceType = sourceType;
    }
}
