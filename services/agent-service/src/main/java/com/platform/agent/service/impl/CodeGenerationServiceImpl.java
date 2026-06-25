package com.platform.agent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.agent.service.CodeGenerationService;
import com.platform.agent.service.ProjectContextService;
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
    private final ProjectContextService projectContextService;

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

    @Override
    public Map<String, String> generateCodeWithProjectContext(Long projectId, String taskDescription) {
        try {
            // 获取项目上下文
            String projectContext = projectContextService.getFullContext(projectId);

            // 合并上下文
            String fullContext = projectContext + "\n\n" + "## 知识库参考\n" + (taskDescription != null ? taskDescription : "");

            String prompt = buildPrompt(fullContext, taskDescription);
            String result = aiService.chat(prompt);

            return parseCodeResult(result);
        } catch (Exception e) {
            log.error("代码生成失败", e);
            throw new RuntimeException("代码生成失败: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String context, String taskDescription) {
        return String.format(
                "你是一个 Java Spring Boot 开发专家。请根据以下项目信息和任务描述，生成相应的代码。\n\n" +
                "## 项目信息\n%s\n\n" +
                "## 任务描述\n%s\n\n" +
                "## 代码生成要求\n" +
                "1. 严格按照项目的包结构和命名规范生成代码\n" +
                "2. 使用项目已有的技术栈（MyBatis-Plus、MapStruct 等）\n" +
                "3. 遵循项目的分层架构：Controller -> Service -> Mapper -> Entity\n" +
                "4. 生成完整可用的代码，包括：\n" +
                "   - Entity 实体类\n" +
                "   - DTO/VO 数据传输对象\n" +
                "   - Mapper 接口\n" +
                "   - Service 接口和实现\n" +
                "   - Controller 控制器\n" +
                "5. 如果需要数据库表，同时生成 SQL 建表语句\n\n" +
                "## 输出格式\n" +
                "请以 JSON 格式返回生成的代码，key 为文件路径，value 为文件内容：\n" +
                "```json\n" +
                "{\n" +
                "  \"src/main/java/com/platform/xxx/entity/Xxx.java\": \"package com.platform.xxx.entity;\\n\\nimport ...\",\n" +
                "  \"src/main/java/com/platform/xxx/service/XxxService.java\": \"package com.platform.xxx.service;\\n\\nimport ...\",\n" +
                "  \"sql/xxx.sql\": \"CREATE TABLE IF NOT EXISTS xxx (...)\"\n" +
                "}\n" +
                "```\n\n" +
                "只返回 JSON，不要添加其他说明。",
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

    @Override
    public Map<String, String> fixCodeErrors(String context, Map<String, String> codeFiles, String errorMsg) {
        try {
            // 构建修复提示
            StringBuilder codeBlock = new StringBuilder();
            for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
                codeBlock.append("### ").append(entry.getKey()).append("\n```java\n");
                codeBlock.append(entry.getValue()).append("\n```\n\n");
            }

            String prompt = String.format(
                    "你是一个 Java Spring Boot 开发专家。以下代码编译失败，请修复错误。\n\n" +
                    "## 项目信息\n%s\n\n" +
                    "## 原始代码\n%s\n\n" +
                    "## 编译错误\n```\n%s\n```\n\n" +
                    "## 修复要求\n" +
                    "1. 仔细分析错误信息，找出根本原因\n" +
                    "2. 修复所有编译错误\n" +
                    "3. 保持代码结构和风格不变\n" +
                    "4. 只修改需要修复的部分\n\n" +
                    "## 输出格式\n" +
                    "请以 JSON 格式返回修复后的完整代码，key 为文件路径，value 为文件内容：\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"src/main/java/...\": \"修复后的完整代码...\"\n" +
                    "}\n" +
                    "```\n\n" +
                    "只返回 JSON，不要添加其他说明。",
                    context, codeBlock.toString(), errorMsg
            );

            String result = aiService.chat(prompt);
            return parseCodeResult(result);
        } catch (Exception e) {
            log.error("代码修复失败", e);
            // 返回原始代码
            return codeFiles;
        }
    }
}
