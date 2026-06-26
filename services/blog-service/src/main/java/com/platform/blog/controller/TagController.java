package com.platform.blog.controller;

import com.platform.blog.domain.dto.TagCreateDTO;
import com.platform.blog.domain.dto.TagUpdateDTO;
import com.platform.blog.domain.vo.TagVO;
import com.platform.blog.service.TagService;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 */
@Tag(name = "标签管理", description = "标签CRUD相关接口")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "获取所有标签列表")
    @GetMapping
    public Result<List<TagVO>> listAll() {
        List<TagVO> list = tagService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<TagVO> getById(@PathVariable Long id) {
        TagVO tagVO = tagService.getById(id);
        return Result.success(tagVO);
    }

    @RequirePermission("blog:tag:add")
    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TagCreateDTO createDTO) {
        Long tagId = tagService.create(createDTO);
        return Result.success(tagId);
    }

    @RequirePermission("blog:tag:edit")
    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TagUpdateDTO updateDTO) {
        tagService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("blog:tag:delete")
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
