package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Bug创建DTO
 */
@Data
public class BugCreateDTO {

    private Long projectId;

    @NotBlank(message = "Bug标题不能为空")
    @Size(max = 200, message = "Bug标题不能超过200个字符")
    private String title;

    @Size(max = 5000, message = "Bug描述不能超过5000个字符")
    private String description;

    private Integer severity;

    private Long assigneeId;

    private LocalDate dueDate;
}
