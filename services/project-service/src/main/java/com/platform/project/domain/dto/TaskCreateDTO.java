package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 任务创建DTO
 */
@Data
public class TaskCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 父任务ID（子任务支持）
     */
    private Long parentTaskId;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200个字符")
    private String title;

    @Size(max = 5000, message = "任务描述不能超过5000个字符")
    private String description;

    /**
     * 优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT
     */
    private Integer priority;

    /**
     * 来源: MANUAL, AI_GENERATED, AGENT_CREATED
     */
    private String sourceType;

    private Long assigneeId;

    private LocalDate dueDate;
}
