package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Bug更新DTO
 */
@Data
public class BugUpdateDTO {

    @NotNull(message = "BugID不能为空")
    private Long id;

    @NotBlank(message = "Bug标题不能为空")
    @Size(max = 200, message = "Bug标题不能超过200个字符")
    private String title;

    @Size(max = 5000, message = "Bug描述不能超过5000个字符")
    private String description;

    private Integer severity;

    private Integer status;

    private Long assigneeId;

    private LocalDate dueDate;
}
