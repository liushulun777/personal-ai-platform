package com.platform.blog.controller;

import com.platform.blog.service.LikeService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞管理控制器
 */
@Tag(name = "点赞管理", description = "点赞相关接口")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "点赞")
    @PostMapping("/{targetId}")
    public Result<Void> like(@PathVariable Long targetId, @RequestParam Integer targetType) {
        likeService.like(targetId, targetType);
        return Result.success();
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{targetId}")
    public Result<Void> unlike(@PathVariable Long targetId, @RequestParam Integer targetType) {
        likeService.unlike(targetId, targetType);
        return Result.success();
    }

    @Operation(summary = "检查是否已点赞")
    @GetMapping("/{targetId}/status")
    public Result<Boolean> isLiked(@PathVariable Long targetId, @RequestParam Integer targetType) {
        boolean liked = likeService.isLiked(targetId, targetType);
        return Result.success(liked);
    }
}
