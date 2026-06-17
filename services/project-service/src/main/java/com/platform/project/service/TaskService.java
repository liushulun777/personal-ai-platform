package com.platform.project.service;

import com.platform.common.core.result.PageResult;
import com.platform.project.domain.dto.TaskCreateDTO;
import com.platform.project.domain.dto.TaskQueryDTO;
import com.platform.project.domain.dto.TaskUpdateDTO;
import com.platform.project.domain.vo.TaskVO;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 分页查询任务
     */
    PageResult<TaskVO> page(TaskQueryDTO queryDTO);

    /**
     * 获取任务详情
     */
    TaskVO getById(Long id);

    /**
     * 创建任务
     */
    Long create(TaskCreateDTO dto);

    /**
     * 更新任务
     */
    void update(TaskUpdateDTO dto);

    /**
     * 删除任务
     */
    void delete(Long id);

    /**
     * 开始任务 (BACKLOG/READY -> DOING)
     */
    void start(Long id);

    /**
     * 完成任务 (DOING/REVIEW -> DONE)
     */
    void complete(Long id);

    /**
     * 分配任务
     */
    void assign(Long id, Long assigneeId);

    /**
     * 阻塞任务 (DOING -> BLOCKED)
     */
    void block(Long id, String reason);

    /**
     * 移动任务状态
     */
    void move(Long id, Integer targetStatus);

    /**
     * 提交审核 (DOING -> REVIEW)
     */
    void submitReview(Long id);

    /**
     * 审核通过 (REVIEW -> DONE)
     */
    void approve(Long id);

    /**
     * 审核拒绝 (REVIEW -> DOING)
     */
    void reject(Long id, String reason);

    /**
     * 解除阻塞 (BLOCKED -> DOING)
     */
    void unblock(Long id);
}
