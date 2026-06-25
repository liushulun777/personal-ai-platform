package com.platform.agent.controller;

import com.platform.agent.service.CodeReviewService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码审查控制器
 */
@Tag(name = "代码审查", description = "AI 代码审查相关接口")
@RestController
@RequestMapping("/code-review")
@RequiredArgsConstructor
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @Operation(summary = "审查代码")
    @PostMapping("/review")
    public Result<Map<String, Object>> reviewCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String language = request.getOrDefault("language", "unknown");
        String context = request.getOrDefault("context", "");

        Map<String, Object> result = codeReviewService.reviewCode(code, language, context);
        return Result.success(result);
    }

    @Operation(summary = "审查文件")
    @PostMapping("/review-file")
    public Result<Map<String, Object>> reviewFile(@RequestBody Map<String, String> request) {
        String filePath = request.get("filePath");
        String content = request.get("content");

        Map<String, Object> result = codeReviewService.reviewFile(filePath, content);
        return Result.success(result);
    }

    @Operation(summary = "批量审查文件")
    @PostMapping("/review-files")
    public Result<List<Map<String, Object>>> reviewFiles(@RequestBody Map<String, String> files) {
        List<Map<String, Object>> results = codeReviewService.reviewFiles(files);
        return Result.success(results);
    }
}
