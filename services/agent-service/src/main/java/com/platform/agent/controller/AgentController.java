package com.platform.agent.controller;

import com.platform.agent.domain.entity.AgentExecution;
import com.platform.agent.service.AgentService;
import com.platform.agent.service.ExecutionHistoryService;
import com.platform.agent.service.TaskTimeoutService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 执行控制器
 */
@Tag(name = "Agent 执行管理", description = "Agent 任务执行相关接口")
@RestController
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final ExecutionHistoryService executionHistoryService;
    private final TaskTimeoutService taskTimeoutService;

    @Operation(summary = "手动触发任务执行")
    @PostMapping("/tasks/{taskId}/execute")
    public Result<Void> executeTask(@PathVariable Long taskId) {
        agentService.executeTask(taskId);
        return Result.success();
    }

    @Operation(summary = "按项目一键执行所有待执行任务")
    @PostMapping("/projects/{projectId}/execute")
    public Result<Integer> executeProjectTasks(@PathVariable Long projectId) {
        int count = agentService.executeProjectTasks(projectId);
        return Result.success(count);
    }

    @Operation(summary = "取消正在执行的任务")
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<Void> cancelTask(@PathVariable Long taskId) {
        boolean cancelled = taskTimeoutService.cancelTask(taskId);
        if (cancelled) {
            return Result.success();
        } else {
            return Result.fail("任务不存在或已完成");
        }
    }

    @Operation(summary = "获取任务执行记录")
    @GetMapping("/tasks/{taskId}/executions")
    public Result<List<AgentExecution>> getTaskExecutions(@PathVariable Long taskId) {
        List<AgentExecution> executions = executionHistoryService.getExecutionsByTaskId(taskId);
        return Result.success(executions);
    }

    @Operation(summary = "获取执行记录详情")
    @GetMapping("/executions/{executionId}")
    public Result<AgentExecution> getExecutionDetail(@PathVariable Long executionId) {
        AgentExecution execution = executionHistoryService.getExecutionDetail(executionId);
        if (execution != null) {
            return Result.success(execution);
        } else {
            return Result.fail("执行记录不存在");
        }
    }

    @Operation(summary = "获取项目执行记录")
    @GetMapping("/projects/{projectId}/executions")
    public Result<List<AgentExecution>> getProjectExecutions(@PathVariable Long projectId) {
        List<AgentExecution> executions = executionHistoryService.getExecutionsByProjectId(projectId);
        return Result.success(executions);
    }

    @Operation(summary = "获取正在执行的任务")
    @GetMapping("/executions/running")
    public Result<List<AgentExecution>> getRunningExecutions() {
        List<AgentExecution> executions = executionHistoryService.getRunningExecutions();
        return Result.success(executions);
    }

    @Operation(summary = "获取执行统计信息")
    @GetMapping("/executions/stats")
    public Result<Object> getExecutionStats() {
        return Result.success(new Object() {
            public final int runningCount = taskTimeoutService.getRunningTaskCount();
        });
    }
}
