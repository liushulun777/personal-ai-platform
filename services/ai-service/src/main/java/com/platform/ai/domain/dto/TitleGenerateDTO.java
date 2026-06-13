package com.platform.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成标题请求DTO
 */
@Data
@Schema(description = "生成标题请求")
public class TitleGenerateDTO {

    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    @Schema(description = "文章内容", required = true)
    private String content;

    /**
     * 标题数量（可选）
     */
    @Schema(description = "标题数量", defaultValue = "5")
    private Integer count = 5;
}
