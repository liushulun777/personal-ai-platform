package com.platform.ai.service.impl;

import com.platform.ai.domain.dto.ArticleGenerateDTO;
import com.platform.ai.domain.vo.ArticleAskVO;
import com.platform.ai.service.AiGenerateService;
import com.platform.common.ai.service.AiService;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    @Override
    public ArticleAskVO askArticle(String articleContent, String question) {
        try {
            String systemPrompt = String.format(
                    "你是一个文章助手。请基于以下文章内容回答用户的问题。如果问题与文章内容无关，请礼貌地告知用户只能回答与文章相关的问题。\n\n文章内容：\n%s",
                    articleContent
            );
            String answer = aiService.chat(question, systemPrompt);
            return ArticleAskVO.builder()
                    .answer(answer)
                    .tokenCount(0)
                    .build();
        } catch (Exception e) {
            log.error("文章问答失败", e);
            throw new BusinessException(ResultCode.FAIL, "问答失败: " + e.getMessage());
        }
    }

    @Override
    public String generateArticle(ArticleGenerateDTO dto) {
        try {
            String prompt = buildArticlePrompt(dto);
            return aiService.chat(prompt);
        } catch (Exception e) {
            log.error("生成文章失败", e);
            throw new BusinessException(ResultCode.FAIL, "生成文章失败: " + e.getMessage());
        }
    }

    @Override
    public SseEmitter generateArticleStream(ArticleGenerateDTO dto) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2分钟超时
        String prompt = buildArticlePrompt(dto);

        CompletableFuture.runAsync(() -> {
            try {
                Flux<String> flux = aiService.streamChat(prompt, null, null);
                flux.subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                log.error("发送SSE数据失败", e);
                            }
                        },
                        error -> {
                            log.error("流式生成文章失败", error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                emitter.send("[DONE]");
                            } catch (IOException ignored) {
                            }
                            emitter.complete();
                        }
                );
            } catch (Exception e) {
                log.error("流式生成文章失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 构建文章生成Prompt
     */
    private String buildArticlePrompt(ArticleGenerateDTO dto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请撰写一篇关于「").append(dto.getTopic()).append("」的文章。\n\n");

        // 文章类型
        switch (dto.getType()) {
            case "tech_blog" -> prompt.append("文章类型：技术博客，包含技术背景、实现细节、代码示例等。\n");
            case "tutorial" -> prompt.append("文章类型：教程，步骤清晰，包含代码示例和说明。\n");
            case "summary" -> prompt.append("文章类型：总结/综述，全面概述相关技术和方案。\n");
            case "essay" -> prompt.append("文章类型：随笔，风格轻松，可以有个人观点。\n");
            default -> prompt.append("文章类型：技术博客。\n");
        }

        // 写作风格
        switch (dto.getStyle()) {
            case "professional" -> prompt.append("写作风格：专业严谨，用词准确。\n");
            case "casual" -> prompt.append("写作风格：轻松活泼，易于阅读。\n");
            case "concise" -> prompt.append("写作风格：简洁明了，直击要点。\n");
            default -> prompt.append("写作风格：专业严谨。\n");
        }

        // 文章长度
        switch (dto.getLength()) {
            case "short" -> prompt.append("文章长度：约500字。\n");
            case "medium" -> prompt.append("文章长度：约1000字。\n");
            case "long" -> prompt.append("文章长度：约2000字。\n");
            default -> prompt.append("文章长度：约1000字。\n");
        }

        // 补充说明
        if (dto.getExtra() != null && !dto.getExtra().isBlank()) {
            prompt.append("\n补充要求：").append(dto.getExtra()).append("\n");
        }

        prompt.append("\n请直接输出 Markdown 格式的文章内容，要求：\n");
        prompt.append("1. 结构清晰，使用合适的标题层级\n");
        prompt.append("2. 内容专业、有深度\n");
        prompt.append("3. 如为技术文章，包含代码示例\n");
        prompt.append("4. 不要添加任何额外说明或前缀");

        return prompt.toString();
    }
}
