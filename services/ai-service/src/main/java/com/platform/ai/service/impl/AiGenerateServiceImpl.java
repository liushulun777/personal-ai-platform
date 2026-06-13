package com.platform.ai.service.impl;

import com.platform.ai.service.AiGenerateService;
import com.platform.common.ai.service.AiService;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerateServiceImpl implements AiGenerateService {

    private final AiService aiService;

    @Override
    public String generateSummary(String content, Integer maxLength) {
        try {
            String prompt = String.format(
                    "请为以下文章生成一个简洁的摘要，摘要长度不超过%d个字符。只返回摘要内容，不要添加任何其他说明。\n\n文章内容：\n%s",
                    maxLength, content
            );
            return aiService.chat(prompt);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            throw new BusinessException(ResultCode.FAIL, "生成摘要失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> generateTags(String title, String content, Integer count) {
        try {
            String prompt = String.format(
                    "请为以下文章生成%d个相关的标签。标签应该是简短的关键词，用逗号分隔。只返回标签列表，不要添加任何其他说明。\n\n标题：%s\n\n内容：\n%s",
                    count, title, content
            );
            String result = aiService.chat(prompt);
            return Arrays.stream(result.split("[,，、]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("生成标签失败", e);
            throw new BusinessException(ResultCode.FAIL, "生成标签失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> generateTitles(String content, Integer count) {
        try {
            String prompt = String.format(
                    "请为以下文章生成%d个吸引人的标题建议。每个标题占一行。只返回标题列表，不要添加任何其他说明。\n\n文章内容：\n%s",
                    count, content
            );
            String result = aiService.chat(prompt);
            return Arrays.stream(result.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("生成标题失败", e);
            throw new BusinessException(ResultCode.FAIL, "生成标题失败: " + e.getMessage());
        }
    }
}
