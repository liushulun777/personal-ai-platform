package com.platform.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文章问答请求DTO
 */
@Data
@Schema(description = "文章问答请求")
public class ArticleAskDTO {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long articleId;

    @NotBlank(message = "问题不能为空")
    @Schema(description = "问题内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String question;
}
