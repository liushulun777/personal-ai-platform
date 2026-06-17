package com.platform.agent.service;

import com.platform.agent.domain.model.TaskExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务重试服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRetryService {

    private final AgentService agentService;
    private final ExecutionHistoryService executionHistoryService;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试间隔（毫秒）：5秒、10秒、20秒
     */
    private static final long[] RETRY_DELAYS = {5000, 10000, 20000};

    /**
     * 任务重试次数记录
     */
    private final ConcurrentHashMap<Long, Integer> retryCountMap = new ConcurrentHashMap<>();

    /**
     * 执行任务（带重试）
     */
    public TaskExecutionResult executeWithRetry(Long taskId) {
        return executeWithRetry(taskId, null);
    }

    /**
     * 执行任务（带重试，带 Token）
     */
    public TaskExecutionResult executeWithRetry(Long taskId, String token) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount <= MAX_RETRY_COUNT) {
            try {
                log.info("执行任务, taskId: {}, retryCount: {}", taskId, retryCount);

                // 执行任务
                agentService.executeTask(taskId, token);

                // 执行成功，清除重试计数
                retryCountMap.remove(taskId);
                return TaskExecutionResult.success("执行成功");

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                retryCountMap.put(taskId, retryCount);

                if (retryCount <= MAX_RETRY_COUNT) {
                    log.warn("任务执行失败，准备重试, taskId: {}, retryCount: {}, error: {}",
                            taskId, retryCount, e.getMessage());

                    // 等待后重试
                    try {
                        long delay = RETRY_DELAYS[Math.min(retryCount - 1, RETRY_DELAYS.length - 1)];
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return TaskExecutionResult.failed("任务被中断");
                    }
                }
            }
        }

        // 重试次数用尽
        log.error("任务重试次数用尽, taskId: {}, maxRetryCount: {}", taskId, MAX_RETRY_COUNT);
        retryCountMap.remove(taskId);
        return TaskExecutionResult.failed("重试次数用尽: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    /**
     * 获取任务重试次数
     */
    public int getRetryCount(Long taskId) {
        return retryCountMap.getOrDefault(taskId, 0);
    }

    /**
     * 重置任务重试计数
     */
    public void resetRetryCount(Long taskId) {
        retryCountMap.remove(taskId);
    }
}
