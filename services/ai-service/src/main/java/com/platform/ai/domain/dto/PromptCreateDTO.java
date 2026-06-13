package com.platform.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Prompt模板创建请求DTO
 */
@Data
@Schema(description = "Prompt模板创建请求")
public class PromptCreateDTO {

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "模板描述")
    private String description;

    @NotBlank(message = "Prompt内容不能为空")
    @Schema(description = "Prompt内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String promptText;

    @Schema(description = "分类")
    private String category;
}
