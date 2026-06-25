package com.platform.agent.service.impl;

import com.platform.agent.service.CodeReviewService;
import com.platform.common.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 代码审查服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final AiService aiService;

    @Override
    public Map<String, Object> reviewCode(String code, String language, String context) {
        log.info("开始代码审查, language={}, codeLength={}", language, code.length());

        String prompt = buildReviewPrompt(code, language, context);
        String result = aiService.chat(prompt);

        return parseReviewResult(result);
    }

    @Override
    public Map<String, Object> reviewFile(String filePath, String content) {
        String language = detectLanguage(filePath);
        String context = "文件路径: " + filePath;
        return reviewCode(content, language, context);
    }

    @Override
    public List<Map<String, Object>> reviewFiles(Map<String, String> files) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<String, String> entry : files.entrySet()) {
            try {
                Map<String, Object> result = reviewFile(entry.getKey(), entry.getValue());
                result.put("filePath", entry.getKey());
                results.add(result);
            } catch (Exception e) {
                log.error("审查文件失败: {}", entry.getKey(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("filePath", entry.getKey());
                errorResult.put("error", e.getMessage());
                results.add(errorResult);
            }
        }

        return results;
    }

    /**
     * 构建审查提示
     */
    private String buildReviewPrompt(String code, String language, String context) {
        return String.format(
                "你是一个资深的代码审查专家。请审查以下代码，找出潜在的问题和改进建议。\n\n" +
                "## 代码信息\n" +
                "- 语言: %s\n" +
                "- 上下文: %s\n\n" +
                "## 代码内容\n" +
                "```%s\n%s\n```\n\n" +
                "## 审查要求\n" +
                "请从以下维度进行审查：\n" +
                "1. **代码质量**: 命名规范、代码结构、可读性\n" +
                "2. **潜在BUG**: 空指针、数组越界、并发问题\n" +
                "3. **性能问题**: 不必要的循环、内存泄漏、SQL优化\n" +
                "4. **安全隐患**: SQL注入、XSS、敏感信息泄露\n" +
                "5. **最佳实践**: 设计模式、异常处理、日志记录\n\n" +
                "## 输出格式\n" +
                "请以 JSON 格式返回审查结果：\n" +
                "```json\n" +
                "{\n" +
                "  \"score\": 85,\n" +
                "  \"level\": \"良好\",\n" +
                "  \"summary\": \"代码整体质量较好，但有一些小问题需要改进\",\n" +
                "  \"issues\": [\n" +
                "    {\n" +
                "      \"severity\": \"warning\",\n" +
                "      \"line\": 10,\n" +
                "      \"message\": \"建议使用 StringBuilder 拼接字符串\",\n" +
                "      \"suggestion\": \"使用 StringBuilder 替代 String 拼接\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"improvements\": [\n" +
                "    \"建议添加单元测试\",\n" +
                "    \"建议提取公共方法\"\n" +
                "  ]\n" +
                "}\n" +
                "```\n\n" +
                "只返回 JSON，不要添加其他说明。",
                language, context, language, code
        );
    }

    /**
     * 解析审查结果
     */
    private Map<String, Object> parseReviewResult(String result) {
        try {
            // 提取 JSON
            int start = result.indexOf('{');
            int end = result.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String json = result.substring(start, end + 1);
                // 简单解析
                Map<String, Object> parsed = new HashMap<>();
                parsed.put("raw", json);

                // 提取分数
                if (json.contains("\"score\"")) {
                    String scoreStr = json.replaceAll(".*\"score\"\\s*:\\s*(\\d+).*", "$1");
                    try {
                        parsed.put("score", Integer.parseInt(scoreStr));
                    } catch (NumberFormatException ignored) {}
                }

                // 提取等级
                if (json.contains("\"level\"")) {
                    String level = json.replaceAll(".*\"level\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                    parsed.put("level", level);
                }

                // 提取摘要
                if (json.contains("\"summary\"")) {
                    String summary = json.replaceAll(".*\"summary\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                    parsed.put("summary", summary);
                }

                return parsed;
            }

            return Map.of("raw", result, "score", 0, "level", "未知", "summary", "解析失败");
        } catch (Exception e) {
            log.error("解析审查结果失败", e);
            return Map.of("raw", result, "score", 0, "level", "错误", "summary", e.getMessage());
        }
    }

    /**
     * 检测编程语言
     */
    private String detectLanguage(String filePath) {
        if (filePath == null) return "unknown";

        String lower = filePath.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".js") || lower.endsWith(".ts")) return "javascript";
        if (lower.endsWith(".vue")) return "vue";
        if (lower.endsWith(".sql")) return "sql";
        if (lower.endsWith(".xml")) return "xml";
        if (lower.endsWith(".yml") || lower.endsWith(".yaml")) return "yaml";
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".md")) return "markdown";

        return "unknown";
    }
}
