package com.platform.agent.service;

import java.util.List;
import java.util.Map;

/**
 * 需求讨论服务 - 引导客户明确需求
 */
public interface RequirementDiscussionService {

    /**
     * 开始需求讨论
     * @param projectId 项目ID
     * @param initialRequirement 初始需求描述
     * @return 讨论会话ID
     */
    String startDiscussion(Long projectId, String initialRequirement);

    /**
     * 继续讨论
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return Agent 回复
     */
    String continueDiscussion(String sessionId, String userMessage);

    /**
     * 生成完整需求文档
     * @param sessionId 会话ID
     * @return 需求文档
     */
    String generateRequirementDocument(String sessionId);

    /**
     * 获取讨论历史
     * @param sessionId 会话ID
     * @return 讨论历史
     */
    List<Map<String, String>> getDiscussionHistory(String sessionId);

    /**
     * 分析需求完整性
     * @param sessionId 会话ID
     * @return 完整性分析结果
     */
    Map<String, Object> analyzeCompleteness(String sessionId);
}
