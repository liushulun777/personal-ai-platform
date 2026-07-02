package com.platform.report.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.report.domain.dto.PermissionCreateDTO;
import com.platform.report.domain.dto.PermissionQueryDTO;
import com.platform.report.domain.dto.PermissionUpdateDTO;
import com.platform.report.domain.vo.PermissionVO;
import com.platform.report.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "报表权限相关接口")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "分页查询权限")
    @GetMapping
    public Result<PageResult<PermissionVO>> page(PermissionQueryDTO queryDTO) {
        PageResult<PermissionVO> result = permissionService.page(queryDTO);
        return Result.success(result);
    }

    @RequirePermission("report:permission:add")
    @Operation(summary = "创建权限")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PermissionCreateDTO createDTO) {
        Long id = permissionService.create(createDTO);
        return Result.success(id);
    }

    @RequirePermission("report:permission:edit")
    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionUpdateDTO updateDTO) {
        permissionService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("report:permission:delete")
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "检查权限")
    @GetMapping("/check")
    public Result<Boolean> checkPermission(
            @RequestParam Long userId,
            @RequestParam Long resourceId,
            @RequestParam String resourceType,
            @RequestParam String permission) {
        boolean hasPermission = permissionService.checkPermission(userId, resourceId, resourceType, permission);
        return Result.success(hasPermission);
    }
}
