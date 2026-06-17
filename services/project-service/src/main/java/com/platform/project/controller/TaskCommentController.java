package com.platform.project.controller;

import com.platform.common.core.result.Result;
import com.platform.project.domain.dto.TaskCommentCreateDTO;
import com.platform.project.domain.vo.TaskCommentVO;
import com.platform.project.service.TaskCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务评论控制器
 */
@Tag(name = "任务评论管理", description = "任务评论相关接口")
@RestController
@RequestMapping("/task-comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TaskCommentCreateDTO dto) {
        Long id = taskCommentService.create(dto);
        return Result.success(id);
    }

    @Operation(summary = "获取任务评论列表")
    @GetMapping("/task/{taskId}")
    public Result<List<TaskCommentVO>> getByTaskId(@PathVariable Long taskId) {
        List<TaskCommentVO> comments = taskCommentService.getByTaskId(taskId);
        return Result.success(comments);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskCommentService.delete(id);
        return Result.success();
    }
}
