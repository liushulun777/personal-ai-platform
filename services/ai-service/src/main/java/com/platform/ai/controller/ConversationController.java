package com.platform.ai.controller;

import com.platform.ai.domain.dto.ChatDTO;
import com.platform.ai.domain.vo.ChatVO;
import com.platform.ai.domain.vo.ConversationListVO;
import com.platform.ai.domain.vo.ConversationVO;
import com.platform.ai.service.ConversationService;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 对话控制器
 */
@Tag(name = "AI对话管理", description = "AI对话相关接口")
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 聊天（同步）
     */
    @Operation(summary = "聊天", description = "发送消息并获取AI回复")
    @PostMapping("/chat")
    public Result<ChatVO> chat(@Valid @RequestBody ChatDTO chatDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(conversationService.chat(userId, chatDTO));
    }

    /**
     * 流式聊天（SSE）
     * 第一个事件为 conversationId，后续为内容片段
     */
    @Operation(summary = "流式聊天", description = "发送消息并获取AI流式回复")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatDTO chatDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return conversationService.streamChat(userId, chatDTO);
    }

    /**
     * 获取对话列表
     */
    @Operation(summary = "获取对话列表", description = "获取当前用户的对话列表")
    @GetMapping
    public Result<PageResult<ConversationListVO>> listConversations(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(conversationService.listConversations(userId, current, size));
    }

    /**
     * 获取对话详情
     */
    @Operation(summary = "获取对话详情", description = "获取对话详情及消息历史")
    @GetMapping("/{conversationId}")
    public Result<ConversationVO> getConversation(
            @Parameter(description = "对话ID", required = true) @PathVariable Long conversationId) {
        return Result.success(conversationService.getConversation(conversationId));
    }

    /**
     * 删除对话
     */
    @Operation(summary = "删除对话", description = "删除对话及所有消息")
    @DeleteMapping("/{conversationId}")
    public Result<Void> deleteConversation(
            @Parameter(description = "对话ID", required = true) @PathVariable Long conversationId) {
        conversationService.deleteConversation(conversationId);
        return Result.success();
    }
}
