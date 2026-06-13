package com.platform.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成摘要请求DTO
 */
@Data
@Schema(description = "生成摘要请求")
public class SummaryDTO {

    /**
     * 原始内容
     */
    @NotBlank(message = "内容不能为空")
    @Schema(description = "原始内容", required = true)
    private String content;

    /**
     * 摘要最大长度（可选）
     */
    @Schema(description = "摘要最大长度", defaultValue = "200")
    private Integer maxLength = 200;
}
