package com.platform.agent.service;

import com.platform.agent.domain.model.TaskExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * 任务超时控制服务
 */
@Slf4j
@Service
public class TaskTimeoutService {

    /**
     * 正在执行的任务（存储 Future 用于取消操作）
     */
    private final ConcurrentHashMap<Long, Future<?>> runningTasks = new ConcurrentHashMap<>();

    /**
     * 执行器
     */
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    /**
     * 执行带超时的任务
     */
    public TaskExecutionResult executeWithTimeout(Long taskId, Callable<TaskExecutionResult> task, Duration timeout) {
        Future<TaskExecutionResult> future = executor.submit(() -> {
            runningTasks.put(taskId, CompletableFuture.completedFuture(null));
            try {
                return task.call();
            } finally {
                runningTasks.remove(taskId);
            }
        });

        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("任务执行超时, taskId: {}, timeout: {}", taskId, timeout);
            future.cancel(true);
            runningTasks.remove(taskId);
            return TaskExecutionResult.timeout();
        } catch (ExecutionException e) {
            log.error("任务执行异常, taskId: {}", taskId, e.getCause());
            return TaskExecutionResult.failed("执行异常: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务被中断, taskId: {}", taskId);
            return TaskExecutionResult.failed("任务被中断");
        }
    }

    /**
     * 取消正在执行的任务
     */
    public boolean cancelTask(Long taskId) {
        Future<?> future = runningTasks.get(taskId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            runningTasks.remove(taskId);
            log.info("取消任务, taskId: {}, result: {}", taskId, cancelled);
            return cancelled;
        }
        return false;
    }

    /**
     * 获取正在执行的任务数量
     */
    public int getRunningTaskCount() {
        return runningTasks.size();
    }

    /**
     * 检查任务是否正在执行
     */
    public boolean isTaskRunning(Long taskId) {
        Future<?> future = runningTasks.get(taskId);
        return future != null && !future.isDone();
    }
}
