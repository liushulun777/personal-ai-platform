package com.platform.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.project.domain.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
