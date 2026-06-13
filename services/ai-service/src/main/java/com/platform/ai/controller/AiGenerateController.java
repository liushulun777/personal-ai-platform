package com.platform.ai.controller;

import com.platform.ai.domain.dto.ArticleAskDTO;
import com.platform.ai.domain.dto.SummaryDTO;
import com.platform.ai.domain.dto.TagGenerateDTO;
import com.platform.ai.domain.dto.TitleGenerateDTO;
import com.platform.ai.domain.vo.ArticleAskVO;
import com.platform.ai.service.AiGenerateService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI生成控制器
 */
@Tag(name = "AI生成管理", description = "AI生成摘要、标签、标题接口")
@RestController
@RequestMapping("/generate")
@RequiredArgsConstructor
public class AiGenerateController {

    private final AiGenerateService aiGenerateService;

    /**
     * 生成摘要
     */
    @Operation(summary = "生成摘要", description = "根据文章内容生成摘要")
    @PostMapping("/summary")
    public Result<String> generateSummary(@Valid @RequestBody SummaryDTO summaryDTO) {
        return Result.success(aiGenerateService.generateSummary(summaryDTO.getContent(), summaryDTO.getMaxLength()));
    }

    /**
     * 生成标签
     */
    @Operation(summary = "生成标签", description = "根据文章标题和内容生成标签")
    @PostMapping("/tags")
    public Result<List<String>> generateTags(@Valid @RequestBody TagGenerateDTO tagGenerateDTO) {
        return Result.success(aiGenerateService.generateTags(
                tagGenerateDTO.getTitle(),
                tagGenerateDTO.getContent(),
                tagGenerateDTO.getCount()
        ));
    }

    /**
     * 生成标题
     */
    @Operation(summary = "生成标题", description = "根据文章内容生成标题建议")
    @PostMapping("/titles")
    public Result<List<String>> generateTitles(@Valid @RequestBody TitleGenerateDTO titleGenerateDTO) {
        return Result.success(aiGenerateService.generateTitles(
                titleGenerateDTO.getContent(),
                titleGenerateDTO.getCount()
        ));
    }

    /**
     * 文章问答
     */
    @Operation(summary = "文章问答", description = "基于文章内容回答用户问题")
    @PostMapping("/ask")
    public Result<ArticleAskVO> askArticle(@Valid @RequestBody ArticleAskDTO articleAskDTO) {
        // TODO: 从 blog-service 获取文章内容
        // 临时使用 question 作为内容
        return Result.success(aiGenerateService.askArticle(
                articleAskDTO.getQuestion(),
                articleAskDTO.getQuestion()
        ));
    }
}
