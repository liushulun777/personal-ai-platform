package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.convert.TaskConvert;
import com.platform.project.domain.dto.TaskCreateDTO;
import com.platform.project.domain.dto.TaskQueryDTO;
import com.platform.project.domain.dto.TaskUpdateDTO;
import com.platform.project.domain.entity.Task;
import com.platform.project.domain.vo.TaskVO;
import com.platform.project.mapper.TaskMapper;
import com.platform.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskConvert taskConvert;

    @Override
    public PageResult<TaskVO> page(TaskQueryDTO queryDTO) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getProjectId() != null, Task::getProjectId, queryDTO.getProjectId());
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Task::getTitle, queryDTO.getTitle());
        wrapper.eq(queryDTO.getStatus() != null, Task::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getPriority() != null, Task::getPriority, queryDTO.getPriority());
        wrapper.eq(queryDTO.getAssigneeId() != null, Task::getAssigneeId, queryDTO.getAssigneeId());
        wrapper.orderByDesc(Task::getCreateTime);

        Page<Task> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Task> result = taskMapper.selectPage(page, wrapper);

        List<TaskVO> records = taskConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public TaskVO getById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        return taskConvert.entityToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskCreateDTO dto) {
        Task task = taskConvert.createDTOToEntity(dto);
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        if (task.getPriority() == null) {
            task.setPriority(1);
        }
        task.setReporterId(SecurityUtils.getCurrentUserIdOrNull());
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TaskUpdateDTO dto) {
        Task task = taskMapper.selectById(dto.getId());
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        taskConvert.updateEntityFromDTO(dto, task);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        taskMapper.deleteById(id);
    }
}
