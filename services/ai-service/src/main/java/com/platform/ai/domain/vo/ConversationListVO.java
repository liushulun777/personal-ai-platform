package com.platform.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话列表VO
 */
@Data
@Schema(description = "对话列表")
public class ConversationListVO {

    /**
     * 对话ID
     */
    @Schema(description = "对话ID")
    private Long id;

    /**
     * 对话标题
     */
    @Schema(description = "对话标题")
    private String title;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称")
    private String model;

    /**
     * 最后一条消息预览
     */
    @Schema(description = "最后一条消息预览")
    private String lastMessage;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
