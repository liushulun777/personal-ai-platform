package com.platform.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.project.domain.entity.Bug;
import org.apache.ibatis.annotations.Mapper;

/**
 * BugMapper
 */
@Mapper
public interface BugMapper extends BaseMapper<Bug> {
}
