package com.platform.blog.controller;

import com.platform.blog.service.FavoriteService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏管理控制器
 */
@Tag(name = "收藏管理", description = "收藏相关接口")
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏文章")
    @PostMapping("/{articleId}")
    public Result<Void> favorite(@PathVariable Long articleId) {
        favoriteService.favorite(articleId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{articleId}")
    public Result<Void> unfavorite(@PathVariable Long articleId) {
        favoriteService.unfavorite(articleId);
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/{articleId}/status")
    public Result<Boolean> isFavorited(@PathVariable Long articleId) {
        boolean favorited = favoriteService.isFavorited(articleId);
        return Result.success(favorited);
    }
}
