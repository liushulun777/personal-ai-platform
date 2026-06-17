package com.platform.resource.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResourceAnalysisVO {
    private List<ServiceResourceVO> serviceResources;
    private Integer totalServices;
    private Double avgCpuUsage;
    private Double avgMemoryUsage;
    private Long highCpuServices;
    private Long highMemoryServices;
}