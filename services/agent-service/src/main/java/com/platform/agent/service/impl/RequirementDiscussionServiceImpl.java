package com.platform.agent.service.impl;

import com.platform.agent.service.ProjectContextService;
import com.platform.agent.service.RequirementDiscussionService;
import com.platform.common.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 需求讨论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementDiscussionServiceImpl implements RequirementDiscussionService {

    private final AiService aiService;
    private final ProjectContextService projectContextService;

    /**
     * 讨论会话存储
     */
    private final ConcurrentHashMap<String, DiscussionSession> sessions = new ConcurrentHashMap<>();

    @Override
    public String startDiscussion(Long projectId, String initialRequirement) {
        String sessionId = UUID.randomUUID().toString();

        // 获取项目上下文
        String projectContext = projectContextService.getFullContext(projectId);

        // 创建讨论会话
        DiscussionSession session = new DiscussionSession();
        session.setProjectId(projectId);
        session.setProjectContext(projectContext);
        session.setInitialRequirement(initialRequirement);
        session.setStartTime(System.currentTimeMillis());

        // 添加初始需求
        session.addMessage("user", initialRequirement);

        // 生成初始分析和引导问题
        String analysis = analyzeRequirement(session);
        session.addMessage("assistant", analysis);

        sessions.put(sessionId, session);
        log.info("开始需求讨论, sessionId: {}, projectId: {}", sessionId, projectId);

        return sessionId;
    }

    @Override
    public String continueDiscussion(String sessionId, String userMessage) {
        DiscussionSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("讨论会话不存在: " + sessionId);
        }

        // 添加用户消息
        session.addMessage("user", userMessage);

        // 生成回复
        String response = generateResponse(session);
        session.addMessage("assistant", response);

        return response;
    }

    @Override
    public String generateRequirementDocument(String sessionId) {
        DiscussionSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("讨论会话不存在: " + sessionId);
        }

        String prompt = buildDocumentGenerationPrompt(session);
        String document = aiService.chat(prompt);

        session.setFinalDocument(document);
        log.info("生成需求文档, sessionId: {}", sessionId);

        return document;
    }

    @Override
    public List<Map<String, String>> getDiscussionHistory(String sessionId) {
        DiscussionSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("讨论会话不存在: " + sessionId);
        }

        return session.getMessages();
    }

    @Override
    public Map<String, Object> analyzeCompleteness(String sessionId) {
        DiscussionSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("讨论会话不存在: " + sessionId);
        }

        String prompt = buildCompletenessAnalysisPrompt(session);
        String analysis = aiService.chat(prompt);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("analysis", analysis);
        result.put("messageCount", session.getMessages().size());

        return result;
    }

    /**
     * 分析初始需求
     */
    private String analyzeRequirement(DiscussionSession session) {
        String prompt = String.format(
                "你是一个资深的需求分析师。请分析以下初始需求，并引导客户进一步明确需求。\n\n" +
                "## 项目信息\n%s\n\n" +
                "## 初始需求\n%s\n\n" +
                "请按照以下格式回复：\n\n" +
                "### 需求理解\n" +
                "简要总结你对需求的理解\n\n" +
                "### 需要明确的问题\n" +
                "列出 3-5 个关键问题，帮助客户进一步明确需求。问题应该具体、有针对性。\n\n" +
                "### 建议的功能点\n" +
                "根据需求，建议可能需要的功能点\n\n" +
                "注意：\n" +
                "1. 问题要具体，不要问太宽泛的问题\n" +
                "2. 结合项目现有技术栈和架构来提问\n" +
                "3. 引导客户思考边界情况和异常处理",
                session.getProjectContext(),
                session.getInitialRequirement()
        );

        return aiService.chat(prompt);
    }

    /**
     * 生成回复
     */
    private String generateResponse(DiscussionSession session) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个资深的需求分析师和项目经理。请继续与客户讨论需求。\n\n");
        prompt.append("## 项目信息\n");
        prompt.append(session.getProjectContext());
        prompt.append("\n\n## 讨论历史\n");

        // 添加讨论历史
        for (Map<String, String> message : session.getMessages()) {
            String role = message.get("role");
            String content = message.get("content");
            if ("user".equals(role)) {
                prompt.append("客户: ").append(content).append("\n\n");
            } else {
                prompt.append("分析师: ").append(content).append("\n\n");
            }
        }

        prompt.append("## 回复要求\n");
        prompt.append("1. 针对客户的回复进行分析和总结\n");
        prompt.append("2. 如果需求还不明确，继续提出具体问题\n");
        prompt.append("3. 如果需求已经比较明确，开始整理需求要点\n");
        prompt.append("4. 结合项目技术栈给出可行性建议\n");
        prompt.append("5. 提醒客户可能遗漏的边界情况\n\n");
        prompt.append("请生成你的回复：");

        return aiService.chat(prompt.toString());
    }

    /**
     * 构建文档生成提示
     */
    private String buildDocumentGenerationPrompt(DiscussionSession session) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个资深的需求分析师。请根据以下讨论内容，生成一份完整的需求文档。\n\n");
        prompt.append("## 项目信息\n");
        prompt.append(session.getProjectContext());
        prompt.append("\n\n## 讨论历史\n");

        for (Map<String, String> message : session.getMessages()) {
            String role = message.get("role");
            String content = message.get("content");
            if ("user".equals(role)) {
                prompt.append("客户: ").append(content).append("\n\n");
            } else {
                prompt.append("分析师: ").append(content).append("\n\n");
            }
        }

        prompt.append("## 输出格式\n");
        prompt.append("请生成 Markdown 格式的需求文档，包含以下章节：\n\n");
        prompt.append("# 需求文档\n\n");
        prompt.append("## 1. 项目概述\n");
        prompt.append("- 项目背景\n");
        prompt.append("- 项目目标\n");
        prompt.append("- 目标用户\n\n");
        prompt.append("## 2. 功能需求\n");
        prompt.append("按模块列出功能需求，每个功能包含：\n");
        prompt.append("- 功能名称\n");
        prompt.append("- 功能描述\n");
        prompt.append("- 输入/输出\n");
        prompt.append("- 业务规则\n\n");
        prompt.append("## 3. 非功能需求\n");
        prompt.append("- 性能要求\n");
        prompt.append("- 安全要求\n");
        prompt.append("- 兼容性要求\n\n");
        prompt.append("## 4. 技术方案建议\n");
        prompt.append("基于项目现有技术栈，给出实现建议\n\n");
        prompt.append("## 5. 任务拆分建议\n");
        prompt.append("将需求拆分为可执行的开发任务，包含执行顺序和依赖关系\n\n");
        prompt.append("请生成完整的需求文档：");

        return prompt.toString();
    }

    /**
     * 构建完整性分析提示
     */
    private String buildCompletenessAnalysisPrompt(DiscussionSession session) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个资深的需求分析师。请分析以下需求讨论的完整性。\n\n");
        prompt.append("## 讨论历史\n");

        for (Map<String, String> message : session.getMessages()) {
            String role = message.get("role");
            String content = message.get("content");
            if ("user".equals(role)) {
                prompt.append("客户: ").append(content).append("\n\n");
            } else {
                prompt.append("分析师: ").append(content).append("\n\n");
            }
        }

        prompt.append("## 分析要求\n");
        prompt.append("请从以下维度分析需求完整性：\n\n");
        prompt.append("1. 功能完整性（0-100分）：功能需求是否清晰完整\n");
        prompt.append("2. 技术可行性（0-100分）：技术方案是否可行\n");
        prompt.append("3. 边界情况（0-100分）：是否考虑了异常和边界情况\n");
        prompt.append("4. 非功能需求（0-100分）：性能、安全等是否明确\n\n");
        prompt.append("## 输出格式\n");
        prompt.append("请以 JSON 格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"functionScore\": 80,\n");
        prompt.append("  \"technicalScore\": 70,\n");
        prompt.append("  \"boundaryScore\": 60,\n");
        prompt.append("  \"nonFunctionScore\": 50,\n");
        prompt.append("  \"overallScore\": 65,\n");
        prompt.append("  \"missingItems\": [\"缺少性能要求\", \"未考虑并发场景\"],\n");
        prompt.append("  \"suggestions\": [\"建议明确响应时间要求\", \"需要定义错误处理策略\"]\n");
        prompt.append("}\n\n");
        prompt.append("请返回分析结果：");

        return aiService.chat(prompt.toString());
    }

    /**
     * 讨论会话内部类
     */
    private static class DiscussionSession {
        private Long projectId;
        private String projectContext;
        private String initialRequirement;
        private List<Map<String, String>> messages = new ArrayList<>();
        private String finalDocument;
        private long startTime;

        public void addMessage(String role, String content) {
            Map<String, String> message = new HashMap<>();
            message.put("role", role);
            message.put("content", content);
            message.put("timestamp", String.valueOf(System.currentTimeMillis()));
            messages.add(message);
        }

        // Getters and Setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectContext() { return projectContext; }
        public void setProjectContext(String projectContext) { this.projectContext = projectContext; }
        public String getInitialRequirement() { return initialRequirement; }
        public void setInitialRequirement(String initialRequirement) { this.initialRequirement = initialRequirement; }
        public List<Map<String, String>> getMessages() { return messages; }
        public String getFinalDocument() { return finalDocument; }
        public void setFinalDocument(String finalDocument) { this.finalDocument = finalDocument; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
    }
}
