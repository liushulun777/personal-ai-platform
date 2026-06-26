package com.platform.project.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.project.domain.dto.BugCreateDTO;
import com.platform.project.domain.dto.BugQueryDTO;
import com.platform.project.domain.dto.BugUpdateDTO;
import com.platform.project.domain.vo.BugVO;
import com.platform.project.service.BugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Bug管理控制器
 */
@Tag(name = "Bug管理", description = "Bug CRUD相关接口")
@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @Operation(summary = "分页查询Bug")
    @GetMapping
    public Result<PageResult<BugVO>> page(BugQueryDTO queryDTO) {
        PageResult<BugVO> result = bugService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取Bug详情")
    @GetMapping("/{id}")
    public Result<BugVO> getById(@PathVariable Long id) {
        BugVO vo = bugService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("project:bug:add")
    @Operation(summary = "创建Bug")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody BugCreateDTO dto) {
        Long id = bugService.create(dto);
        return Result.success(id);
    }

    @RequirePermission("project:bug:edit")
    @Operation(summary = "更新Bug")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody BugUpdateDTO dto) {
        bugService.update(dto);
        return Result.success();
    }

    @RequirePermission("project:bug:delete")
    @Operation(summary = "删除Bug")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bugService.delete(id);
        return Result.success();
    }
}
