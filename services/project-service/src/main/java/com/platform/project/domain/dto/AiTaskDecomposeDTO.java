package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI任务拆解请求DTO
 */
@Data
public class AiTaskDecomposeDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "任务内容不能为空")
    private String content;

    /**
     * 项目技术栈描述（可选，用于更精准拆分）
     */
    private String techStack;

    /**
     * 最大任务数量限制（可选）
     */
    private Integer maxTasks;

    /**
     * 任务粒度: fine-细粒度, medium-中等, coarse-粗粒度
     */
    private String granularity;
}
