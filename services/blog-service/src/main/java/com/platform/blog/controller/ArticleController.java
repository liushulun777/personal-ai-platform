package com.platform.blog.controller;

import com.platform.blog.domain.dto.ArticleCreateDTO;
import com.platform.blog.domain.dto.ArticleQueryDTO;
import com.platform.blog.domain.dto.ArticleUpdateDTO;
import com.platform.blog.domain.vo.ArticleDetailVO;
import com.platform.blog.domain.vo.ArticleVO;
import com.platform.blog.service.ArticleService;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理控制器
 */
@Tag(name = "文章管理", description = "文章CRUD相关接口")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "分页查询文章")
    @GetMapping
    public Result<PageResult<ArticleVO>> page(ArticleQueryDTO queryDTO) {
        PageResult<ArticleVO> result = articleService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getById(@PathVariable Long id) {
        ArticleDetailVO detailVO = articleService.getById(id);
        return Result.success(detailVO);
    }

    @RequirePermission("blog:article:add")
    @Operation(summary = "创建文章")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO createDTO) {
        Long articleId = articleService.create(createDTO);
        return Result.success(articleId);
    }

    @RequirePermission("blog:article:edit")
    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleUpdateDTO updateDTO) {
        articleService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("blog:article:delete")
    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @RequirePermission("blog:article:publish")
    @Operation(summary = "发布文章")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publish(id);
        return Result.success();
    }

    @RequirePermission("blog:article:publish")
    @Operation(summary = "归档文章")
    @PutMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        articleService.archive(id);
        return Result.success();
    }
}
