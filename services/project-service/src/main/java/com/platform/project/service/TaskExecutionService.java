package com.platform.project.service;

import com.platform.project.domain.dto.TaskExecutionCreateDTO;
import com.platform.project.domain.vo.TaskExecutionVO;

import java.util.List;

/**
 * 任务执行记录服务接口
 */
public interface TaskExecutionService {

    /**
     * 创建执行记录
     */
    Long create(TaskExecutionCreateDTO dto);

    /**
     * 获取任务的执行记录列表
     */
    List<TaskExecutionVO> getByTaskId(Long taskId);
}
