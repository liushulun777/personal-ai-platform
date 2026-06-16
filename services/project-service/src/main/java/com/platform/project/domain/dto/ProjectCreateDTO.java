package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 项目创建DTO
 */
@Data
public class ProjectCreateDTO {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String name;

    @Size(max = 2000, message = "项目描述不能超过2000个字符")
    private String description;

    private Integer priority;

    private Long ownerId;

    private LocalDate startDate;

    private LocalDate endDate;
}
