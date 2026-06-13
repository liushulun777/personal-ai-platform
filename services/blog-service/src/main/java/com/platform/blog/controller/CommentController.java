package com.platform.blog.controller;

import com.platform.blog.domain.dto.CommentCreateDTO;
import com.platform.blog.domain.vo.CommentVO;
import com.platform.blog.service.CommentService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理控制器
 */
@Tag(name = "评论管理", description = "评论相关接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取文章评论列表")
    @GetMapping("/article/{articleId}")
    public Result<List<CommentVO>> listByArticleId(@PathVariable Long articleId) {
        List<CommentVO> list = commentService.listByArticleId(articleId);
        return Result.success(list);
    }

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CommentCreateDTO createDTO) {
        Long commentId = commentService.create(createDTO);
        return Result.success(commentId);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
