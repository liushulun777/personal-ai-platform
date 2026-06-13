package com.platform.common.ai.service;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI服务接口
 * 所有业务模块必须通过此接口调用AI能力，禁止直接调用大模型SDK
 */
public interface AiService {

    /**
     * 聊天（使用默认模型）
     *
     * @param prompt 提示词
     * @return 回复内容
     */
    String chat(String prompt);

    /**
     * 带上下文的聊天（使用默认模型）
     *
     * @param prompt   提示词
     * @param context  上下文
     * @return 回复内容
     */
    String chat(String prompt, String context);

    /**
     * 使用指定模型聊天
     *
     * @param prompt     提示词
     * @param context    上下文
     * @param modelName  模型名称（如 mimo, openai）
     * @return 回复内容
     */
    String chat(String prompt, String context, String modelName);

    /**
     * 流式聊天
     *
     * @param prompt    提示词
     * @param context   上下文
     * @param modelName 模型名称
     * @return 流式回复内容
     */
    Flux<String> streamChat(String prompt, String context, String modelName);

    /**
     * 生成摘要
     *
     * @param content 原始内容
     * @return 摘要
     */
    String generateSummary(String content);

    /**
     * 生成标签
     *
     * @param title   标题
     * @param content 内容
     * @return 标签列表
     */
    List<String> generateTags(String title, String content);

    /**
     * 生成标题
     *
     * @param content 内容
     * @return 标题建议
     */
    List<String> generateTitles(String content);
}
