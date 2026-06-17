package com.platform.agent.client;

import com.platform.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Project Service Feign Client
 */
@FeignClient(name = "project-service")
public interface ProjectServiceClient {

    /**
     * 移动任务状态
     */
    @PostMapping("/tasks/{id}/move")
    Result<Void> moveTask(@PathVariable("id") Long id, @RequestParam("targetStatus") Integer targetStatus);

    /**
     * 开始任务
     */
    @PostMapping("/tasks/{id}/start")
    Result<Void> startTask(@PathVariable("id") Long id);

    /**
     * 完成任务
     */
    @PostMapping("/tasks/{id}/complete")
    Result<Void> completeTask(@PathVariable("id") Long id);

    /**
     * 阻塞任务
     */
    @PostMapping("/tasks/{id}/block")
    Result<Void> blockTask(@PathVariable("id") Long id, @RequestParam("reason") String reason);

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{id}")
    Result<Object> getTask(@PathVariable("id") Long id);
}
