package com.platform.project.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQueryDTO extends PageQuery {

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 任务标题（模糊查询）
     */
    private String title;

    /**
     * 状态: 0-BACKLOG, 1-READY, 2-DOING, 3-REVIEW, 4-DONE, 5-BLOCKED
     */
    private Integer status;

    /**
     * 优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT
     */
    private Integer priority;

    /**
     * 执行人ID
     */
    private Long assigneeId;

    /**
     * 来源类型: MANUAL, AI_GENERATED, AGENT_CREATED
     */
    private String sourceType;
}
