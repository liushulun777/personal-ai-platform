package com.platform.monitoring.dto;

import lombok.Data;

/**
 * 单个服务资源使用DTO
 */
@Data
public class ServiceResourceUsageDto {

    /**
     * 微服务名称
     */
    private String serviceName;

    /**
     * 实例数量
     */
    private Integer instanceCount;

    /**
     * 平均CPU使用率
     */
    private Double avgCpuUsage;

    /**
     * 平均内存使用率
     */
    private Double avgMemoryUsage;

    /**
     * 总内存使用量 (MB)
     */
    private Long totalMemoryUsedMb;

    /**
     * 最大CPU使用率（所有实例中）
     */
    private Double maxCpuUsage;

    /**
     * 最大内存使用率（所有实例中）
     */
    private Double maxMemoryUsage;

    /**
     * 资源消耗风险等级
     */
    private String riskLevel; // LOW, MEDIUM, HIGH
}