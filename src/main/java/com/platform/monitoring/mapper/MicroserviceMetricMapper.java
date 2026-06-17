package com.platform.monitoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.monitoring.domain.MicroserviceMetric;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微服务资源指标Mapper
 */
@Mapper
public interface MicroserviceMetricMapper extends BaseMapper<MicroserviceMetric> {
}