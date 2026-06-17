package com.platform.resource.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceResourceVO {
    private String serviceName;
    private Double avgCpuUsage;
    private Double avgMemoryUsage;
    private Integer sampleCount;
    private LocalDateTime lastUpdateTime;
}