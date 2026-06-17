package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.convert.TaskConvert;
import com.platform.project.domain.dto.TaskCreateDTO;
import com.platform.project.domain.dto.TaskExecutionCreateDTO;
import com.platform.project.domain.dto.TaskQueryDTO;
import com.platform.project.domain.dto.TaskUpdateDTO;
import com.platform.project.domain.entity.Task;
import com.platform.project.domain.vo.TaskVO;
import com.platform.project.event.TaskEventPublisher;
import com.platform.project.mapper.TaskMapper;
import com.platform.project.service.TaskExecutionService;
import com.platform.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 任务服务实现
 *
 * 状态机:
 * BACKLOG(0) -> READY(1) -> DOING(2) -> REVIEW(3) -> DONE(4)
 *                  |                      |
 *                  v                      v
 *              BLOCKED(5) <----------- BLOCKED(5)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskConvert taskConvert;
    private final TaskExecutionService taskExecutionService;
    private final TaskEventPublisher taskEventPublisher;

    @Override
    public PageResult<TaskVO> page(TaskQueryDTO queryDTO) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getProjectId() != null, Task::getProjectId, queryDTO.getProjectId());
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Task::getTitle, queryDTO.getTitle());
        wrapper.eq(queryDTO.getStatus() != null, Task::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getPriority() != null, Task::getPriority, queryDTO.getPriority());
        wrapper.eq(queryDTO.getAssigneeId() != null, Task::getAssigneeId, queryDTO.getAssigneeId());
        wrapper.eq(StringUtils.hasText(queryDTO.getSourceType()), Task::getSourceType, queryDTO.getSourceType());
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
            task.setStatus(0); // BACKLOG
        }
        if (task.getPriority() == null) {
            task.setPriority(1); // MEDIUM
        }
        if (task.getSourceType() == null) {
            task.setSourceType("MANUAL");
        }
        task.setReporterId(SecurityUtils.getCurrentUserIdOrNull());
        taskMapper.insert(task);

        // 记录执行日志
        createExecution(task.getId(), "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "CREATE", "创建任务: " + task.getTitle(), null, null, 0, null, null);

        // 事务提交后发送任务创建事件
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                taskEventPublisher.publishTaskCreatedEvent(task);
            }
        });

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

        // 记录执行日志
        createExecution(task.getId(), "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "UPDATE", "更新任务: " + task.getTitle(), null, null, 0, null, null);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 BACKLOG 或 READY 状态可以开始
        if (task.getStatus() != 0 && task.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有待规划或可执行状态的任务可以开始");
        }

        task.setStatus(2); // DOING
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "START", "开始执行任务", null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 DOING 或 REVIEW 状态可以完成
        if (task.getStatus() != 2 && task.getStatus() != 3) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有执行中或待审核状态的任务可以完成");
        }

        task.setStatus(4); // DONE
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "COMPLETE", "完成任务", null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long id, Long assigneeId) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        task.setAssigneeId(assigneeId);
        // 如果是 BACKLOG 状态，自动转为 READY
        if (task.getStatus() == 0) {
            task.setStatus(1);
        }
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "ASSIGN", "分配任务给用户: " + assigneeId, null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void block(Long id, String reason) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 DOING 状态可以阻塞
        if (task.getStatus() != 2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有执行中的任务可以阻塞");
        }

        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "阻塞原因不能为空");
        }

        task.setStatus(5); // BLOCKED
        task.setBlockedReason(reason);
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "BLOCK", "阻塞任务, 原因: " + reason, null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(Long id, Integer targetStatus) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        if (!isValidTransition(task.getStatus(), targetStatus)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    String.format("不允许从状态 %d 转换到状态 %d", task.getStatus(), targetStatus));
        }

        task.setStatus(targetStatus);
        // 如果移动到非阻塞状态，清除阻塞原因
        if (targetStatus != 5) {
            task.setBlockedReason(null);
        }
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "MOVE", "移动任务状态到: " + getStatusName(targetStatus), null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 DOING 状态可以提交审核
        if (task.getStatus() != 2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有执行中的任务可以提交审核");
        }

        task.setStatus(3); // REVIEW
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "SUBMIT_REVIEW", "提交任务审核", null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 REVIEW 状态可以审核通过
        if (task.getStatus() != 3) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有待审核状态的任务可以审核通过");
        }

        task.setStatus(4); // DONE
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "APPROVE", "审核通过", null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reason) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 REVIEW 状态可以审核拒绝
        if (task.getStatus() != 3) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有待审核状态的任务可以审核拒绝");
        }

        task.setStatus(2); // 回到 DOING
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "REJECT", "审核拒绝, 原因: " + reason, null, null, 0, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblock(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }

        // 只有 BLOCKED 状态可以解除阻塞
        if (task.getStatus() != 5) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有阻塞状态的任务可以解除阻塞");
        }

        task.setStatus(2); // 回到 DOING
        task.setBlockedReason(null);
        taskMapper.updateById(task);

        createExecution(id, "HUMAN", SecurityUtils.getCurrentUserIdOrNull(),
                "UNBLOCK", "解除任务阻塞", null, null, 0, null, null);
    }

    /**
     * 验证状态转换是否合法
     */
    private boolean isValidTransition(Integer from, Integer to) {
        // BACKLOG -> READY, DOING
        if (from == 0) return to == 1 || to == 2;
        // READY -> DOING, BLOCKED
        if (from == 1) return to == 2 || to == 5;
        // DOING -> REVIEW, DONE, BLOCKED
        if (from == 2) return to == 3 || to == 4 || to == 5;
        // REVIEW -> DONE, DOING
        if (from == 3) return to == 4 || to == 2;
        // DONE -> 无（终态）
        if (from == 4) return false;
        // BLOCKED -> DOING
        if (from == 5) return to == 2;
        return false;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "BACKLOG";
            case 1 -> "READY";
            case 2 -> "DOING";
            case 3 -> "REVIEW";
            case 4 -> "DONE";
            case 5 -> "BLOCKED";
            default -> "UNKNOWN";
        };
    }

    /**
     * 创建执行记录
     */
    private void createExecution(Long taskId, String executorType, Long executorId,
                                  String action, String content, String prompt, String result,
                                  Integer status, String errorMsg, Long duration) {
        try {
            TaskExecutionCreateDTO dto = new TaskExecutionCreateDTO();
            dto.setTaskId(taskId);
            dto.setExecutorType(executorType);
            dto.setExecutorId(executorId);
            dto.setAction(action);
            dto.setContent(content);
            dto.setPrompt(prompt);
            dto.setResult(result);
            dto.setStatus(status);
            dto.setErrorMsg(errorMsg);
            dto.setDuration(duration);
            taskExecutionService.create(dto);
        } catch (Exception e) {
            log.error("创建执行记录失败", e);
        }
    }
}
