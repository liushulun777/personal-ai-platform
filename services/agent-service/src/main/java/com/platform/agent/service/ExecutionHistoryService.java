package com.platform.agent.service;

import com.platform.agent.domain.entity.AgentExecution;
import com.platform.agent.domain.entity.ExecutionStep;

import java.util.List;

/**
 * 执行历史服务接口
 */
public interface ExecutionHistoryService {

    /**
     * 创建执行记录
     */
    Long createExecution(Long taskId);

    /**
     * 开始执行
     */
    void startExecution(Long executionId);

    /**
     * 完成执行
     */
    void completeExecution(Long executionId, String resultSummary);

    /**
     * 执行失败
     */
    void failExecution(Long executionId, String errorMessage);

    /**
     * 执行超时
     */
    void timeoutExecution(Long executionId);

    /**
     * 取消执行
     */
    void cancelExecution(Long executionId);

    /**
     * 添加执行步骤
     */
    Long addStep(Long executionId, String stepName, Integer stepOrder);

    /**
     * 更新步骤状态
     */
    void updateStepStatus(Long stepId, Integer status, String detail);

    /**
     * 步骤完成
     */
    void completeStep(Long stepId, String detail);

    /**
     * 步骤失败
     */
    void failStep(Long stepId, String errorMessage);

    /**
     * 查询执行记录详情
     */
    AgentExecution getExecutionDetail(Long executionId);

    /**
     * 查询任务的执行记录
     */
    List<AgentExecution> getExecutionsByTaskId(Long taskId);

    /**
     * 查询项目的执行记录
     */
    List<AgentExecution> getExecutionsByProjectId(Long projectId);

    /**
     * 查询正在执行的任务
     */
    List<AgentExecution> getRunningExecutions();
}
