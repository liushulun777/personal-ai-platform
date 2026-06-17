package com.platform.agent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.agent.service.CodeGenerationService;
import com.platform.common.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGenerationServiceImpl implements CodeGenerationService {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, String> generateCode(String context, String taskDescription) {
        try {
            String prompt = buildPrompt(context, taskDescription);
            String result = aiService.chat(prompt);

            // 解析 AI 返回的 JSON
            return parseCodeResult(result);
        } catch (Exception e) {
            log.error("代码生成失败", e);
            throw new RuntimeException("代码生成失败: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String context, String taskDescription) {
        return String.format(
                "你是一个 Java Spring Boot 开发专家。请根据以下上下文和任务描述，生成相应的代码。\n\n" +
                "## 上下文（现有代码参考）\n%s\n\n" +
                "## 任务描述\n%s\n\n" +
                "请以 JSON 格式返回生成的代码，格式如下：\n" +
                "```json\n" +
                "{\n" +
                "  \"src/main/java/com/platform/xxx/XxxController.java\": \"文件内容...\",\n" +
                "  \"src/main/java/com/platform/xxx/XxxService.java\": \"文件内容...\"\n" +
                "}\n" +
                "```\n\n" +
                "要求：\n" +
                "1. 代码风格与上下文保持一致\n" +
                "2. 使用 MyBatis-Plus、MapStruct 等项目已有的技术栈\n" +
                "3. 遵循项目的命名规范和分层架构\n" +
                "4. 只返回 JSON，不要添加其他说明",
                context, taskDescription
        );
    }

    private Map<String, String> parseCodeResult(String result) {
        try {
            // 提取 JSON 部分
            String json = extractJson(result);
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("解析代码结果失败: {}", result, e);
            return new HashMap<>();
        }
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}
