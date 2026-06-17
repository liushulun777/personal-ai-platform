package com.platform.monitoring.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 微服务资源分析结果DTO
 */
@Data
public class MicroserviceResourceAnalysisDto {

    /**
     * 分析时间
     */
    private String analysisTime;

    /**
     * 微服务总数
     */
    private Integer totalServices;

    /**
     * 按CPU使用率排序的高资源消耗服务列表（前N名）
     */
    private List<ServiceResourceUsageDto> highCpuConsumers;

    /**
     * 按内存使用率排序的高资源消耗服务列表（前N名）
     */
    private List<ServiceResourceUsageDto> highMemoryConsumers;

    /**
     * 所有服务的资源使用概览（按服务名聚合）
     */
    private Map<String, ServiceResourceUsageDto> serviceOverview;
}