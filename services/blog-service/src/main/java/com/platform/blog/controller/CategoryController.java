package com.platform.blog.controller;

import com.platform.blog.domain.dto.CategoryCreateDTO;
import com.platform.blog.domain.dto.CategoryUpdateDTO;
import com.platform.blog.domain.vo.CategoryVO;
import com.platform.blog.service.CategoryService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 */
@Tag(name = "分类管理", description = "分类CRUD相关接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类列表")
    @GetMapping
    public Result<List<CategoryVO>> listAll() {
        List<CategoryVO> list = categoryService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getById(@PathVariable Long id) {
        CategoryVO categoryVO = categoryService.getById(id);
        return Result.success(categoryVO);
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CategoryCreateDTO createDTO) {
        Long categoryId = categoryService.create(createDTO);
        return Result.success(categoryId);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateDTO updateDTO) {
        categoryService.update(id, updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
