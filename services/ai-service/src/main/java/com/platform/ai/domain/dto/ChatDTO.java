package com.platform.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
@Schema(description = "聊天请求")
public class ChatDTO {

    /**
     * 对话ID（可选，为空则创建新对话）
     */
    @Schema(description = "对话ID")
    private Long conversationId;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息", required = true)
    private String message;

    /**
     * 模型名称（可选）
     * 可选值: mimo, openai
     */
    @Schema(description = "模型名称", defaultValue = "mimo", allowableValues = {"mimo", "openai"})
    private String model;
}
