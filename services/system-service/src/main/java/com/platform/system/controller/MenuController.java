package com.platform.system.controller;

import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.system.domain.dto.MenuCreateDTO;
import com.platform.system.domain.dto.MenuUpdateDTO;
import com.platform.system.domain.vo.MenuVO;
import com.platform.system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理", description = "菜单CRUD相关接口")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @RequirePermission("system:menu:list")
    @Operation(summary = "获取菜单树形列表")
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        List<MenuVO> tree = menuService.tree();
        return Result.success(tree);
    }

    @RequirePermission("system:menu:list")
    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public Result<MenuVO> getById(@PathVariable Long id) {
        MenuVO menuVO = menuService.getById(id);
        return Result.success(menuVO);
    }

    @RequirePermission("system:menu:add")
    @Operation(summary = "创建菜单")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody MenuCreateDTO createDTO) {
        Long menuId = menuService.create(createDTO);
        return Result.success(menuId);
    }

    @RequirePermission("system:menu:edit")
    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuUpdateDTO updateDTO) {
        menuService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("system:menu:delete")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
