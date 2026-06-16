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
     * 状态
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 执行人ID
     */
    private Long assigneeId;
}
