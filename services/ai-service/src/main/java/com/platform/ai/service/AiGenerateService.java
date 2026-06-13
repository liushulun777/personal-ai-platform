package com.platform.ai.service;

import java.util.List;

/**
 * AI生成服务接口
 * 提供摘要、标签、标题生成能力
 */
public interface AiGenerateService {

    /**
     * 生成摘要
     *
     * @param content   原始内容
     * @param maxLength 最大长度
     * @return 摘要
     */
    String generateSummary(String content, Integer maxLength);

    /**
     * 生成标签
     *
     * @param title   文章标题
     * @param content 文章内容
     * @param count   标签数量
     * @return 标签列表
     */
    List<String> generateTags(String title, String content, Integer count);

    /**
     * 生成标题
     *
     * @param content 文章内容
     * @param count   标题数量
     * @return 标题列表
     */
    List<String> generateTitles(String content, Integer count);
}
