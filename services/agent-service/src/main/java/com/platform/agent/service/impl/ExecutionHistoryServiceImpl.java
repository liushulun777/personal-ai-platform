package com.platform.agent.service.impl;

import com.platform.agent.domain.entity.AgentExecution;
import com.platform.agent.domain.entity.ExecutionStep;
import com.platform.agent.mapper.AgentExecutionMapper;
import com.platform.agent.mapper.ExecutionStepMapper;
import com.platform.agent.service.ExecutionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 执行历史服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionHistoryServiceImpl implements ExecutionHistoryService {

    private final AgentExecutionMapper executionMapper;
    private final ExecutionStepMapper stepMapper;

    @Override
    @Transactional
    public Long createExecution(Long taskId) {
        AgentExecution execution = AgentExecution.builder()
                .taskId(taskId)
                .status(0) // 待执行
                .executorId(getExecutorId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        executionMapper.insert(execution);
        log.info("创建执行记录, executionId: {}, taskId: {}", execution.getId(), taskId);
        return execution.getId();
    }

    @Override
    @Transactional
    public void startExecution(Long executionId) {
        AgentExecution execution = new AgentExecution();
        execution.setId(executionId);
        execution.setStatus(1); // 执行中
        execution.setStartTime(LocalDateTime.now());
        execution.setUpdateTime(LocalDateTime.now());
        executionMapper.updateById(execution);
        log.info("开始执行, executionId: {}", executionId);
    }

    @Override
    @Transactional
    public void completeExecution(Long executionId, String resultSummary) {
        AgentExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return;
        }

        long duration = 0;
        if (execution.getStartTime() != null) {
            duration = java.time.Duration.between(execution.getStartTime(), LocalDateTime.now()).toMillis();
        }

        execution.setStatus(2); // 成功
        execution.setEndTime(LocalDateTime.now());
        execution.setDuration(duration);
        execution.setResultSummary(resultSummary);
        execution.setUpdateTime(LocalDateTime.now());
        executionMapper.updateById(execution);
        log.info("执行完成, executionId: {}, duration: {}ms", executionId, duration);
    }

    @Override
    @Transactional
    public void failExecution(Long executionId, String errorMessage) {
        AgentExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return;
        }

        long duration = 0;
        if (execution.getStartTime() != null) {
            duration = java.time.Duration.between(execution.getStartTime(), LocalDateTime.now()).toMillis();
        }

        execution.setStatus(3); // 失败
        execution.setEndTime(LocalDateTime.now());
        execution.setDuration(duration);
        execution.setErrorMessage(errorMessage);
        execution.setUpdateTime(LocalDateTime.now());
        executionMapper.updateById(execution);
        log.error("执行失败, executionId: {}, error: {}", executionId, errorMessage);
    }

    @Override
    @Transactional
    public void timeoutExecution(Long executionId) {
        AgentExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return;
        }

        execution.setStatus(4); // 超时
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage("任务执行超时");
        execution.setUpdateTime(LocalDateTime.now());
        executionMapper.updateById(execution);
        log.warn("执行超时, executionId: {}", executionId);
    }

    @Override
    @Transactional
    public void cancelExecution(Long executionId) {
        AgentExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return;
        }

        execution.setStatus(5); // 取消
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage("任务被取消");
        execution.setUpdateTime(LocalDateTime.now());
        executionMapper.updateById(execution);
        log.info("执行取消, executionId: {}", executionId);
    }

    @Override
    @Transactional
    public Long addStep(Long executionId, String stepName, Integer stepOrder) {
        ExecutionStep step = ExecutionStep.builder()
                .executionId(executionId)
                .stepName(stepName)
                .stepOrder(stepOrder)
                .status(0) // 待执行
                .createTime(LocalDateTime.now())
                .build();

        stepMapper.insert(step);
        log.debug("添加执行步骤, executionId: {}, stepName: {}, stepOrder: {}", executionId, stepName, stepOrder);
        return step.getId();
    }

    @Override
    @Transactional
    public void updateStepStatus(Long stepId, Integer status, String detail) {
        ExecutionStep step = new ExecutionStep();
        step.setId(stepId);
        step.setStatus(status);
        step.setDetail(detail);
        if (status == 1) {
            step.setStartTime(LocalDateTime.now());
        }
        stepMapper.updateById(step);
    }

    @Override
    @Transactional
    public void completeStep(Long stepId, String detail) {
        ExecutionStep step = stepMapper.selectById(stepId);
        if (step == null) {
            return;
        }

        long duration = 0;
        if (step.getStartTime() != null) {
            duration = java.time.Duration.between(step.getStartTime(), LocalDateTime.now()).toMillis();
        }

        step.setStatus(2); // 成功
        step.setEndTime(LocalDateTime.now());
        step.setDuration(duration);
        step.setDetail(detail);
        stepMapper.updateById(step);
        log.debug("步骤完成, stepId: {}, duration: {}ms", stepId, duration);
    }

    @Override
    @Transactional
    public void failStep(Long stepId, String errorMessage) {
        ExecutionStep step = stepMapper.selectById(stepId);
        if (step == null) {
            return;
        }

        long duration = 0;
        if (step.getStartTime() != null) {
            duration = java.time.Duration.between(step.getStartTime(), LocalDateTime.now()).toMillis();
        }

        step.setStatus(3); // 失败
        step.setEndTime(LocalDateTime.now());
        step.setDuration(duration);
        step.setErrorMessage(errorMessage);
        stepMapper.updateById(step);
        log.error("步骤失败, stepId: {}, error: {}", stepId, errorMessage);
    }

    @Override
    public AgentExecution getExecutionDetail(Long executionId) {
        AgentExecution execution = executionMapper.selectById(executionId);
        if (execution != null) {
            List<ExecutionStep> steps = stepMapper.selectByExecutionId(executionId);
            execution.setSteps(steps);
        }
        return execution;
    }

    @Override
    public List<AgentExecution> getExecutionsByTaskId(Long taskId) {
        List<AgentExecution> executions = executionMapper.selectByTaskId(taskId);
        for (AgentExecution execution : executions) {
            List<ExecutionStep> steps = stepMapper.selectByExecutionId(execution.getId());
            execution.setSteps(steps);
        }
        return executions;
    }

    @Override
    public List<AgentExecution> getExecutionsByProjectId(Long projectId) {
        return executionMapper.selectByProjectId(projectId);
    }

    @Override
    public List<AgentExecution> getRunningExecutions() {
        return executionMapper.selectRunning();
    }

    /**
     * 获取执行器实例 ID
     */
    private String getExecutorId() {
        String hostname;
        try {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "unknown";
        }
        return hostname + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
