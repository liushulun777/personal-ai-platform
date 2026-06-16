package com.platform.project.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.project.domain.dto.TaskCreateDTO;
import com.platform.project.domain.dto.TaskQueryDTO;
import com.platform.project.domain.dto.TaskUpdateDTO;
import com.platform.project.domain.vo.TaskVO;
import com.platform.project.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理", description = "任务CRUD相关接口")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "分页查询任务")
    @GetMapping
    public Result<PageResult<TaskVO>> page(TaskQueryDTO queryDTO) {
        PageResult<TaskVO> result = taskService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskVO> getById(@PathVariable Long id) {
        TaskVO vo = taskService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TaskCreateDTO dto) {
        Long id = taskService.create(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新任务")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody TaskUpdateDTO dto) {
        taskService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }
}
