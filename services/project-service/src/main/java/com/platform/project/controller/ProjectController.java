package com.platform.project.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.project.domain.dto.ProjectCreateDTO;
import com.platform.project.domain.dto.ProjectQueryDTO;
import com.platform.project.domain.dto.ProjectUpdateDTO;
import com.platform.project.domain.vo.ProjectVO;
import com.platform.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 项目管理控制器
 */
@Tag(name = "项目管理", description = "项目CRUD相关接口")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "分页查询项目")
    @GetMapping
    public Result<PageResult<ProjectVO>> page(ProjectQueryDTO queryDTO) {
        PageResult<ProjectVO> result = projectService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<ProjectVO> getById(@PathVariable Long id) {
        ProjectVO vo = projectService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建项目")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProjectCreateDTO dto) {
        Long id = projectService.create(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新项目")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProjectUpdateDTO dto) {
        projectService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }
}
