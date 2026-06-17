package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.project.convert.TaskExecutionConvert;
import com.platform.project.domain.dto.TaskExecutionCreateDTO;
import com.platform.project.domain.entity.TaskExecution;
import com.platform.project.domain.vo.TaskExecutionVO;
import com.platform.project.mapper.TaskExecutionMapper;
import com.platform.project.service.TaskExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务执行记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutionServiceImpl implements TaskExecutionService {

    private final TaskExecutionMapper taskExecutionMapper;
    private final TaskExecutionConvert taskExecutionConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskExecutionCreateDTO dto) {
        TaskExecution execution = taskExecutionConvert.createDTOToEntity(dto);
        taskExecutionMapper.insert(execution);
        log.info("创建任务执行记录, taskId: {}, action: {}", dto.getTaskId(), dto.getAction());
        return execution.getId();
    }

    @Override
    public List<TaskExecutionVO> getByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskExecution::getTaskId, taskId);
        wrapper.orderByDesc(TaskExecution::getCreateTime);
        List<TaskExecution> executions = taskExecutionMapper.selectList(wrapper);
        return taskExecutionConvert.entityListToVOList(executions);
    }
}
