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
}
