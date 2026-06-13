package com.platform.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成标签请求DTO
 */
@Data
@Schema(description = "生成标签请求")
public class TagGenerateDTO {

    /**
     * 文章标题
     */
    @NotBlank(message = "标题不能为空")
    @Schema(description = "文章标题", required = true)
    private String title;

    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    @Schema(description = "文章内容", required = true)
    private String content;

    /**
     * 标签数量（可选）
     */
    @Schema(description = "标签数量", defaultValue = "5")
    private Integer count = 5;
}
