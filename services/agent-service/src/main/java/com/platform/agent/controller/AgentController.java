package com.platform.agent.controller;

import com.platform.agent.service.AgentService;
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
}
