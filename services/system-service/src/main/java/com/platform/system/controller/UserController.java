package com.platform.system.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.system.domain.dto.UserCreateDTO;
import com.platform.system.domain.dto.UserQueryDTO;
import com.platform.system.domain.dto.UserStatusDTO;
import com.platform.system.domain.dto.UserUpdateDTO;
import com.platform.system.domain.vo.UserDetailVO;
import com.platform.system.domain.vo.UserVO;
import com.platform.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户CRUD相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<PageResult<UserVO>> page(UserQueryDTO queryDTO) {
        PageResult<UserVO> result = userService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVO> getById(@PathVariable Long id) {
        UserDetailVO detailVO = userService.getById(id);
        return Result.success(detailVO);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateDTO createDTO) {
        Long userId = userService.create(createDTO);
        return Result.success(userId);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO updateDTO) {
        userService.update(id, updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO statusDTO) {
        userService.updateStatus(id, statusDTO);
        return Result.success();
    }

    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
