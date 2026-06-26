package com.platform.project.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.project.domain.dto.TaskCreateDTO;
import com.platform.project.domain.dto.TaskQueryDTO;
import com.platform.project.domain.dto.TaskUpdateDTO;
import com.platform.project.domain.vo.TaskExecutionVO;
import com.platform.project.domain.vo.TaskVO;
import com.platform.project.service.TaskExecutionService;
import com.platform.project.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理", description = "任务CRUD及状态流转接口")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskExecutionService taskExecutionService;

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

    @RequirePermission("project:task:add")
    @Operation(summary = "创建任务")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TaskCreateDTO dto) {
        Long id = taskService.create(dto);
        return Result.success(id);
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "更新任务")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody TaskUpdateDTO dto) {
        taskService.update(dto);
        return Result.success();
    }

    @RequirePermission("project:task:delete")
    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    // ==================== 任务流转接口 ====================

    @RequirePermission("project:task:edit")
    @Operation(summary = "开始任务", description = "BACKLOG/READY -> DOING")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        taskService.start(id);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "完成任务", description = "DOING/REVIEW -> DONE")
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        taskService.complete(id);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "分配任务")
    @PostMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        taskService.assign(id, assigneeId);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "阻塞任务", description = "DOING -> BLOCKED")
    @PostMapping("/{id}/block")
    public Result<Void> block(@PathVariable Long id, @RequestParam String reason) {
        taskService.block(id, reason);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "移动任务状态")
    @PostMapping("/{id}/move")
    public Result<Void> move(@PathVariable Long id, @RequestParam Integer targetStatus) {
        taskService.move(id, targetStatus);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "提交审核", description = "DOING -> REVIEW")
    @PostMapping("/{id}/submit-review")
    public Result<Void> submitReview(@PathVariable Long id) {
        taskService.submitReview(id);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "审核通过", description = "REVIEW -> DONE")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        taskService.approve(id);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "审核拒绝", description = "REVIEW -> DOING")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        taskService.reject(id, reason);
        return Result.success();
    }

    @RequirePermission("project:task:edit")
    @Operation(summary = "解除阻塞", description = "BLOCKED -> DOING")
    @PostMapping("/{id}/unblock")
    public Result<Void> unblock(@PathVariable Long id) {
        taskService.unblock(id);
        return Result.success();
    }

    // ==================== 执行记录接口 ====================

    @Operation(summary = "获取任务执行记录")
    @GetMapping("/{id}/executions")
    public Result<List<TaskExecutionVO>> getExecutions(@PathVariable Long id) {
        List<TaskExecutionVO> executions = taskExecutionService.getByTaskId(id);
        return Result.success(executions);
    }
}
