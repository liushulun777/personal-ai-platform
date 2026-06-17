package com.platform.monitoring.controller;

import com.platform.monitoring.dto.MicroserviceResourceAnalysisDto;
import com.platform.monitoring.dto.ServiceResourceUsageDto;
import com.platform.monitoring.service.IMicroserviceMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 微服务资源监控控制器
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MicroserviceMetricController {

    private final IMicroserviceMetricService microserviceMetricService;

    /**
     * 分析微服务资源使用情况
     *
     * @return 分析结果
     */
    @GetMapping("/resource-analysis")
    public ResponseEntity<MicroserviceResourceAnalysisDto> analyzeResourceUsage() {
        MicroserviceResourceAnalysisDto analysis = microserviceMetricService.analyzeResourceUsage();
        return ResponseEntity.ok(analysis);
    }

    /**
     * 获取资源消耗排名
     *
     * @param metric 排序指标: CPU 或 MEMORY
     * @param topN   前N名
     * @return 排名列表
     */
    @GetMapping("/top-consumers")
    public ResponseEntity<List<ServiceResourceUsageDto>> getTopConsumers(
            @RequestParam(value = "metric", defaultValue = "CPU") String metric,
            @RequestParam(value = "topN", defaultValue = "5") int topN) {
        List<ServiceResourceUsageDto> topConsumers = microserviceMetricService.getTopConsumers(metric, topN);
        return ResponseEntity.ok(topConsumers);
    }
}