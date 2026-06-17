package com.platform.resource.service.impl;

import com.platform.common.exception.BusinessException;
import com.platform.resource.convert.ResourceConvert;
import com.platform.resource.domain.dto.ResourceAnalysisDTO;
import com.platform.resource.domain.entity.ServiceResourceEntity;
import com.platform.resource.domain.vo.ServiceResourceVO;
import com.platform.resource.domain.vo.ResourceAnalysisVO;
import com.platform.resource.mapper.ServiceResourceMapper;
import com.platform.resource.service.ResourceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceAnalysisServiceImpl implements ResourceAnalysisService {

    private final ServiceResourceMapper serviceResourceMapper;
    private final ResourceConvert resourceConvert;

    @Override
    public ResourceAnalysisVO analyzeResource(ResourceAnalysisDTO dto) {
        // 获取指定时间范围内的资源数据
        List<ServiceResourceEntity> entities = serviceResourceMapper.selectByTimeRange(
                dto.getStartTime(), dto.getEndTime());

        if (entities.isEmpty()) {
            throw new BusinessException("未找到资源使用数据");
        }

        return buildAnalysisVO(entities);
    }

    @Override
    public List<ServiceResourceVO> listServiceResources() {
        List<ServiceResourceEntity> entities = serviceResourceMapper.selectList(null);
        return resourceConvert.entityListToVoList(entities);
    }

    @Override
    public List<ServiceResourceVO> getHighConsumptionServices(Double cpuThreshold, Double memoryThreshold) {
        // 获取最新数据
        List<ServiceResourceEntity> latestEntities = serviceResourceMapper.selectLatestByService();

        return latestEntities.stream()
                .filter(entity -> entity.getCpuUsage() > cpuThreshold || entity.getMemoryUsage() > memoryThreshold)
                .map(resourceConvert::entityToVo)
                .collect(Collectors.toList());
    }

    @Override
    public ResourceAnalysisVO getResourceSummary() {
        // 获取最新数据
        List<ServiceResourceEntity> entities = serviceResourceMapper.selectLatestByService();

        if (entities.isEmpty()) {
            throw new BusinessException("未找到资源使用数据");
        }

        return buildAnalysisVO(entities);
    }

    private ResourceAnalysisVO buildAnalysisVO(List<ServiceResourceEntity> entities) {
        // 按服务名分组统计
        Map<String, List<ServiceResourceEntity>> serviceGroups = entities.stream()
                .collect(Collectors.groupingBy(ServiceResourceEntity::getServiceName));

        // 计算每个服务的平均资源使用
        List<ServiceResourceVO> serviceResources = serviceGroups.entrySet().stream()
                .map(entry -> {
                    List<ServiceResourceEntity> groupEntities = entry.getValue();
                    ServiceResourceVO vo = new ServiceResourceVO();
                    vo.setServiceName(entry.getKey());
                    vo.setAvgCpuUsage(groupEntities.stream()
                            .mapToDouble(ServiceResourceEntity::getCpuUsage)
                            .average()
                            .orElse(0.0));
                    vo.setAvgMemoryUsage(groupEntities.stream()
                            .mapToDouble(ServiceResourceEntity::getMemoryUsage)
                            .average()
                            .orElse(0.0));
                    vo.setSampleCount(groupEntities.size());
                    vo.setLastUpdateTime(groupEntities.stream()
                            .map(ServiceResourceEntity::getRecordTime)
                            .max(LocalDateTime::compareTo)
                            .orElse(null));
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建分析结果
        ResourceAnalysisVO analysisVO = new ResourceAnalysisVO();
        analysisVO.setServiceResources(serviceResources);
        analysisVO.setTotalServices(serviceResources.size());
        analysisVO.setAvgCpuUsage(serviceResources.stream()
                .mapToDouble(ServiceResourceVO::getAvgCpuUsage)
                .average()
                .orElse(0.0));
        analysisVO.setAvgMemoryUsage(serviceResources.stream()
                .mapToDouble(ServiceResourceVO::getAvgMemoryUsage)
                .average()
                .orElse(0.0));
        analysisVO.setHighCpuServices(serviceResources.stream()
                .filter(vo -> vo.getAvgCpuUsage() > 80.0)
                .count());
        analysisVO.setHighMemoryServices(serviceResources.stream()
                .filter(vo -> vo.getAvgMemoryUsage() > 80.0)
                .count());

        return analysisVO;
    }
}