package com.platform.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.project.domain.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务依赖关系Mapper
 */
@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {
}
