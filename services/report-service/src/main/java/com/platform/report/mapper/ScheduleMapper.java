package com.platform.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.report.domain.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度任务Mapper
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}
