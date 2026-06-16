package com.platform.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.project.domain.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
