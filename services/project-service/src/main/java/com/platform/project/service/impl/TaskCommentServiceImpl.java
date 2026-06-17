package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.convert.TaskCommentConvert;
import com.platform.project.domain.dto.TaskCommentCreateDTO;
import com.platform.project.domain.entity.TaskComment;
import com.platform.project.domain.vo.TaskCommentVO;
import com.platform.project.mapper.TaskCommentMapper;
import com.platform.project.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentMapper taskCommentMapper;
    private final TaskCommentConvert taskCommentConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskCommentCreateDTO dto) {
        TaskComment comment = taskCommentConvert.createDTOToEntity(dto);
        comment.setUserId(SecurityUtils.getCurrentUserIdOrNull());
        comment.setIsAiSummary(0);
        taskCommentMapper.insert(comment);
        log.info("创建任务评论, taskId: {}", dto.getTaskId());
        return comment.getId();
    }

    @Override
    public List<TaskCommentVO> getByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId);
        wrapper.orderByDesc(TaskComment::getCreateTime);
        List<TaskComment> comments = taskCommentMapper.selectList(wrapper);
        return taskCommentConvert.entityListToVOList(comments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TaskComment comment = taskCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评论不存在");
        }
        taskCommentMapper.deleteById(id);
    }
}
