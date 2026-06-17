package com.platform.monitoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.monitoring.domain.MicroserviceMetric;
import com.platform.monitoring.dto.MicroserviceResourceAnalysisDto;
import com.platform.monitoring.dto.ServiceResourceUsageDto;
import com.platform.monitoring.mapper.MicroserviceMetricMapper;
import com.platform.monitoring.service.IMicroserviceMetricService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微服务资源指标服务实现
 */
@Service
public class MicroserviceMetricServiceImpl
        extends ServiceImpl<MicroserviceMetricMapper, MicroserviceMetric>
        implements IMicroserviceMetricService {

    // CPU使用率阈值，超过此值视为高消耗
    private static final double CPU_HIGH_THRESHOLD = 70.0;
    // 内存使用率阈值，超过此值视为高消耗
    private static final double MEMORY_HIGH_THRESHOLD = 80.0;

    @Override
    public MicroserviceResourceAnalysisDto analyzeResourceUsage() {
        MicroserviceResourceAnalysisDto analysisResult = new MicroserviceResourceAnalysisDto();
        analysisResult.setAnalysisTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 1. 查询所有微服务指标数据（假设分析最近一次的数据，实际可能需要更复杂的聚合）
        // 这里为了演示，我们从数据库获取所有记录，实际项目可能需要时间范围、聚合计算等
        List<MicroserviceMetric> allMetrics = list(new QueryWrapper<MicroserviceMetric>()
                .orderByDesc("collect_time")
                .last("LIMIT 1000") // 限制记录数，防止内存溢出，实际应有更精确的查询
        );

        if (allMetrics.isEmpty()) {
            analysisResult.setTotalServices(0);
            analysisResult.setHighCpuConsumers(Collections.emptyList());
            analysisResult.setHighMemoryConsumers(Collections.emptyList());
            analysisResult.setServiceOverview(Collections.emptyMap());
            return analysisResult;
        }

        // 2. 按服务名分组
        Map<String, List<MicroserviceMetric>> serviceGroup = allMetrics.stream()
                .collect(Collectors.groupingBy(MicroserviceMetric::getServiceName));

        analysisResult.setTotalServices(serviceGroup.size());

        // 3. 计算每个服务的资源使用统计
        Map<String, ServiceResourceUsageDto> serviceOverview = new HashMap<>();
        List<ServiceResourceUsageDto> allServiceUsages = new ArrayList<>();

        for (Map.Entry<String, List<MicroserviceMetric>> entry : serviceGroup.entrySet()) {
            String serviceName = entry.getKey();
            List<MicroserviceMetric> metrics = entry.getValue();

            ServiceResourceUsageDto usageDto = calculateServiceUsage(serviceName, metrics);
            serviceOverview.put(serviceName, usageDto);
            allServiceUsages.add(usageDto);
        }

        // 4. 识别高资源消耗服务
        // 按CPU使用率排序，取前5名
        List<ServiceResourceUsageDto> highCpuConsumers = allServiceUsages.stream()
                .sorted(Comparator.comparingDouble(ServiceResourceUsageDto::getAvgCpuUsage).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // 按内存使用率排序，取前5名
        List<ServiceResourceUsageDto> highMemoryConsumers = allServiceUsages.stream()
                .sorted(Comparator.comparingDouble(ServiceResourceUsageDto::getAvgMemoryUsage).reversed())
                .limit(5)
                .collect(Collectors.toList());

        analysisResult.setHighCpuConsumers(highCpuConsumers);
        analysisResult.setHighMemoryConsumers(highMemoryConsumers);
        analysisResult.setServiceOverview(serviceOverview);

        return analysisResult;
    }

    @Override
    public List<ServiceResourceUsageDto> getTopConsumers(String metric, int topN) {
        List<MicroserviceMetric> allMetrics = list(); // 实际应查询最新数据
        if (allMetrics.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<MicroserviceMetric>> serviceGroup = allMetrics.stream()
                .collect(Collectors.groupingBy(MicroserviceMetric::getServiceName));

        List<ServiceResourceUsageDto> allServiceUsages = new ArrayList<>();
        for (Map.Entry<String, List<MicroserviceMetric>> entry : serviceGroup.entrySet()) {
            allServiceUsages.add(calculateServiceUsage(entry.getKey(), entry.getValue()));
        }

        Comparator<ServiceResourceUsageDto> comparator;
        if ("CPU".equalsIgnoreCase(metric)) {
            comparator = Comparator.comparingDouble(ServiceResourceUsageDto::getAvgCpuUsage);
        } else {
            comparator = Comparator.comparingDouble(ServiceResourceUsageDto::getAvgMemoryUsage);
        }

        return allServiceUsages.stream()
                .sorted(comparator.reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 计算单个服务的资源使用统计
     */
    private ServiceResourceUsageDto calculateServiceUsage(String serviceName, List<MicroserviceMetric> metrics) {
        ServiceResourceUsageDto dto = new ServiceResourceUsageDto();
        dto.setServiceName(serviceName);
        dto.setInstanceCount(metrics.size());

        // 计算平均值
        DoubleSummaryStatistics cpuStats = metrics.stream()
                .mapToDouble(MicroserviceMetric::getCpuUsage)
                .summaryStatistics();
        DoubleSummaryStatistics memoryStats = metrics.stream()
                .mapToDouble(MicroserviceMetric::getMemoryUsage)
                .summaryStatistics();

        dto.setAvgCpuUsage(cpuStats.getAverage());
        dto.setAvgMemoryUsage(memoryStats.getAverage());
        dto.setMaxCpuUsage(cpuStats.getMax());
        dto.setMaxMemoryUsage(memoryStats.getMax());

        // 计算总内存使用量
        long totalMemoryUsedMb = metrics.stream()
                .mapToLong(MicroserviceMetric::getMemoryUsedMb)
                .sum();
        dto.setTotalMemoryUsedMb(totalMemoryUsedMb);

        // 评估风险等级
        dto.setRiskLevel(evaluateRiskLevel(cpuStats.getAverage(), memoryStats.getAverage()));

        return dto;
    }

    /**
     * 评估资源消耗风险等级
     */
    private String evaluateRiskLevel(double avgCpu, double avgMemory) {
        if (avgCpu > CPU_HIGH_THRESHOLD || avgMemory > MEMORY_HIGH_THRESHOLD) {
            return "HIGH";
        } else if (avgCpu > (CPU_HIGH_THRESHOLD * 0.7) || avgMemory > (MEMORY_HIGH_THRESHOLD * 0.7)) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}