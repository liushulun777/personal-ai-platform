package com.platform.system.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.system.domain.dto.RoleCreateDTO;
import com.platform.system.domain.dto.RoleQueryDTO;
import com.platform.system.domain.dto.RoleUpdateDTO;
import com.platform.system.domain.vo.RoleDetailVO;
import com.platform.system.domain.vo.RoleVO;
import com.platform.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色CRUD相关接口")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色")
    @GetMapping
    public Result<PageResult<RoleVO>> page(RoleQueryDTO queryDTO) {
        PageResult<RoleVO> result = roleService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/all")
    public Result<List<RoleVO>> listAll() {
        List<RoleVO> list = roleService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleDetailVO> getById(@PathVariable Long id) {
        RoleDetailVO detailVO = roleService.getById(id);
        return Result.success(detailVO);
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleCreateDTO createDTO) {
        Long roleId = roleService.create(createDTO);
        return Result.success(roleId);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO updateDTO) {
        roleService.update(id, updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "分配菜单")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
}
