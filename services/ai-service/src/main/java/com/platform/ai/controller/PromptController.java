package com.platform.ai.controller;

import com.platform.ai.domain.dto.PromptCreateDTO;
import com.platform.ai.domain.vo.PromptVO;
import com.platform.ai.service.PromptService;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Prompt模板控制器
 */
@Tag(name = "Prompt模板管理", description = "Prompt模板CRUD接口")
@RestController
@RequestMapping("/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    /**
     * 分页查询Prompt模板
     */
    @Operation(summary = "分页查询", description = "分页查询Prompt模板列表")
    @GetMapping
    public Result<PageResult<PromptVO>> listPrompts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(promptService.listPrompts(current, size));
    }

    /**
     * 按分类查询Prompt模板
     */
    @Operation(summary = "按分类查询", description = "按分类查询Prompt模板")
    @GetMapping("/category/{category}")
    public Result<List<PromptVO>> listByCategory(@PathVariable String category) {
        return Result.success(promptService.listByCategory(category));
    }

    /**
     * 获取所有分类
     */
    @Operation(summary = "获取分类", description = "获取所有Prompt分类及数量")
    @GetMapping("/categories")
    public Result<Map<String, Long>> getCategories() {
        return Result.success(promptService.getCategories());
    }

    /**
     * 创建Prompt模板
     */
    @Operation(summary = "创建模板", description = "创建新的Prompt模板")
    @PostMapping
    public Result<PromptVO> createPrompt(@Valid @RequestBody PromptCreateDTO dto) {
        return Result.success(promptService.createPrompt(dto));
    }

    /**
     * 更新Prompt模板
     */
    @Operation(summary = "更新模板", description = "更新指定的Prompt模板")
    @PutMapping("/{id}")
    public Result<PromptVO> updatePrompt(@PathVariable Long id, @Valid @RequestBody PromptCreateDTO dto) {
        return Result.success(promptService.updatePrompt(id, dto));
    }

    /**
     * 删除Prompt模板
     */
    @Operation(summary = "删除模板", description = "删除指定的Prompt模板")
    @DeleteMapping("/{id}")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return Result.success();
    }
}
