package com.platform.agent.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionResult {

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 工作区路径
     */
    private String workspacePath;

    /**
     * 创建成功结果
     */
    public static TaskExecutionResult success(String message) {
        return TaskExecutionResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static TaskExecutionResult failed(String message) {
        return TaskExecutionResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 创建超时结果
     */
    public static TaskExecutionResult timeout() {
        return TaskExecutionResult.builder()
                .success(false)
                .message("任务执行超时")
                .build();
    }
}
