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
}
