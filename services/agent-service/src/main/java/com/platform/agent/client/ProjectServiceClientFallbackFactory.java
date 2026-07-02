package com.platform.agent.client;

import com.platform.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * ProjectServiceClient 降级工厂
 */
@Slf4j
@Component
public class ProjectServiceClientFallbackFactory implements FallbackFactory<ProjectServiceClient> {

    @Override
    public ProjectServiceClient create(Throwable cause) {
        return new ProjectServiceClient() {
            @Override
            public Result<Void> moveTask(Long id, Integer targetStatus) {
                log.error("移动任务状态失败，任务ID: {}, 目标状态: {}, 原因: {}", id, targetStatus, cause.getMessage());
                return Result.fail("任务状态更新失败，请稍后重试");
            }

            @Override
            public Result<Void> startTask(Long id) {
                log.error("开始任务失败，任务ID: {}, 原因: {}", id, cause.getMessage());
                return Result.fail("任务开始失败，请稍后重试");
            }

            @Override
            public Result<Void> completeTask(Long id) {
                log.error("完成任务失败，任务ID: {}, 原因: {}", id, cause.getMessage());
                return Result.fail("任务完成失败，请稍后重试");
            }

            @Override
            public Result<Void> blockTask(Long id, String reason) {
                log.error("阻塞任务失败，任务ID: {}, 原因: {}", id, cause.getMessage());
                return Result.fail("任务阻塞失败，请稍后重试");
            }

            @Override
            public Result<Object> getTask(Long id) {
                log.error("获取任务详情失败，任务ID: {}, 原因: {}", id, cause.getMessage());
                return Result.fail("获取任务详情失败，请稍后重试");
            }

            @Override
            public Result<Map<String, Object>> getTaskList(Long projectId, Integer status, Integer size) {
                log.error("获取任务列表失败，项目ID: {}, 原因: {}", projectId, cause.getMessage());
                return Result.success(Collections.emptyMap());
            }
        };
    }
}