package com.platform.agent.service;

/**
 * Agent 执行服务接口
 */
public interface AgentService {

    /**
     * 执行任务
     *
     * @param taskId 任务ID
     */
    void executeTask(Long taskId);

    /**
     * 执行任务（带 Token）
     *
     * @param taskId 任务ID
     * @param token  认证Token
     */
    void executeTask(Long taskId, String token);

    /**
     * 按项目执行所有待执行任务
     *
     * @param projectId 项目ID
     * @return 执行的任务数量
     */
    int executeProjectTasks(Long projectId);
}
