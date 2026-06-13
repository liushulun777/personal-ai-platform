package com.platform.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话详情VO
 */
@Data
@Schema(description = "对话详情")
public class ConversationVO {

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
     * 消息列表
     */
    @Schema(description = "消息列表")
    private List<MessageVO> messages;

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
