package com.platform.project.service;

import com.platform.project.domain.dto.TaskCommentCreateDTO;
import com.platform.project.domain.vo.TaskCommentVO;

import java.util.List;

/**
 * 任务评论服务接口
 */
public interface TaskCommentService {

    /**
     * 创建评论
     */
    Long create(TaskCommentCreateDTO dto);

    /**
     * 获取任务的评论列表
     */
    List<TaskCommentVO> getByTaskId(Long taskId);

    /**
     * 删除评论
     */
    void delete(Long id);
}
