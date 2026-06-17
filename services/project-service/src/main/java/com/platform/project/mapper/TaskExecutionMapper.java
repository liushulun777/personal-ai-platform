package com.platform.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.project.domain.entity.TaskExecution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务执行记录Mapper
 */
@Mapper
public interface TaskExecutionMapper extends BaseMapper<TaskExecution> {
}
