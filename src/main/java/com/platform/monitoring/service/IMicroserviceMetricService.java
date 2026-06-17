package com.platform.monitoring.service;

import com.platform.monitoring.dto.MicroserviceResourceAnalysisDto;
import com.platform.monitoring.dto.ServiceResourceUsageDto;

import java.util.List;

/**
 * 微服务资源指标服务接口
 */
public interface IMicroserviceMetricService {

    /**
     * 分析微服务资源使用情况，识别高资源消耗服务
     *
     * @return 分析结果
     */
    MicroserviceResourceAnalysisDto analyzeResourceUsage();

    /**
     * 获取资源消耗排名前N的服务
     *
     * @param metric 排序指标 (CPU, MEMORY)
     * @param topN   前N名
     * @return 服务列表
     */
    List<ServiceResourceUsageDto> getTopConsumers(String metric, int topN);
}