package com.platform.common.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.common.core.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}
