package com.platform.agent.controller;

import com.platform.agent.service.RequirementDiscussionService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 需求讨论控制器
 */
@Tag(name = "需求讨论管理", description = "需求讨论相关接口")
@RestController
@RequestMapping("/requirement")
@RequiredArgsConstructor
public class RequirementDiscussionController {

    private final RequirementDiscussionService requirementDiscussionService;

    @Operation(summary = "开始需求讨论", description = "创建需求讨论会话，Agent 会分析需求并引导讨论")
    @PostMapping("/discussions/start")
    public Result<Map<String, Object>> startDiscussion(
            @RequestParam Long projectId,
            @RequestParam String initialRequirement) {
        String sessionId = requirementDiscussionService.startDiscussion(projectId, initialRequirement);

        Map<String, Object> result = Map.of(
                "sessionId", sessionId,
                "message", "需求讨论已开始，请继续描述您的需求细节"
        );
        return Result.success(result);
    }

    @Operation(summary = "继续需求讨论", description = "在讨论会话中继续与 Agent 对话")
    @PostMapping("/discussions/{sessionId}/continue")
    public Result<Map<String, String>> continueDiscussion(
            @PathVariable String sessionId,
            @RequestParam String message) {
        String response = requirementDiscussionService.continueDiscussion(sessionId, message);

        Map<String, String> result = Map.of(
                "sessionId", sessionId,
                "response", response
        );
        return Result.success(result);
    }

    @Operation(summary = "生成需求文档", description = "根据讨论内容生成完整的需求文档")
    @PostMapping("/discussions/{sessionId}/generate-document")
    public Result<Map<String, String>> generateDocument(@PathVariable String sessionId) {
        String document = requirementDiscussionService.generateRequirementDocument(sessionId);

        Map<String, String> result = Map.of(
                "sessionId", sessionId,
                "document", document
        );
        return Result.success(result);
    }

    @Operation(summary = "获取讨论历史", description = "获取讨论会话的历史消息")
    @GetMapping("/discussions/{sessionId}/history")
    public Result<List<Map<String, String>>> getDiscussionHistory(@PathVariable String sessionId) {
        List<Map<String, String>> history = requirementDiscussionService.getDiscussionHistory(sessionId);
        return Result.success(history);
    }

    @Operation(summary = "分析需求完整性", description = "分析当前需求讨论的完整性")
    @GetMapping("/discussions/{sessionId}/analyze")
    public Result<Map<String, Object>> analyzeCompleteness(@PathVariable String sessionId) {
        Map<String, Object> analysis = requirementDiscussionService.analyzeCompleteness(sessionId);
        return Result.success(analysis);
    }
}
