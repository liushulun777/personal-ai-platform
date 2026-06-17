package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 任务更新DTO
 */
@Data
public class TaskUpdateDTO {

    @NotNull(message = "任务ID不能为空")
    private Long id;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200个字符")
    private String title;

    @Size(max = 5000, message = "任务描述不能超过5000个字符")
    private String description;

    /**
     * 状态: 0-BACKLOG, 1-READY, 2-DOING, 3-REVIEW, 4-DONE, 5-BLOCKED
     */
    private Integer status;

    /**
     * 优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT
     */
    private Integer priority;

    private Long assigneeId;

    private LocalDate dueDate;
}
