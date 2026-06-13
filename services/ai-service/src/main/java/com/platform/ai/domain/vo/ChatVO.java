package com.platform.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天响应VO
 */
@Data
@Schema(description = "聊天响应")
public class ChatVO {

    /**
     * 对话ID
     */
    @Schema(description = "对话ID")
    private Long conversationId;

    /**
     * AI回复
     */
    @Schema(description = "AI回复")
    private String reply;

    /**
     * Token使用量
     */
    @Schema(description = "Token使用量")
    private Integer tokenCount;
}
