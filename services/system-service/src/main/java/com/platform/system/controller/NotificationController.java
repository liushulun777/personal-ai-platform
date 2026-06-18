package com.platform.system.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.util.SecurityUtils;
import com.platform.system.domain.entity.Notification;
import com.platform.system.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息通知控制器
 */
@Tag(name = "消息通知", description = "消息通知相关接口")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<PageResult<Notification>> list(
            @RequestParam(required = false) Integer readStatus,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResult<Notification> result = notificationService.listByUserId(userId, readStatus, current, size);
        return Result.success(result);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        int count = notificationService.countUnread(userId);
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "标记所有消息为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.delete(id, userId);
        return Result.success();
    }
}
