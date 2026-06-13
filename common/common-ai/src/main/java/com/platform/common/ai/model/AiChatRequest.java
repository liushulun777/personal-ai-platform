package com.platform.common.ai.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI聊天请求
 */
@Data
public class AiChatRequest implements Serializable {

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 上下文消息列表
     */
    private List<String> context;

    /**
     * 模型名称（可选）
     */
    private String model;

    /**
     * 温度参数（可选）
     */
    private Double temperature;

    /**
     * 最大token数（可选）
     */
    private Integer maxTokens;
}
