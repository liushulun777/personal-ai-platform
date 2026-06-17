package com.platform.resource.service;

import com.platform.resource.domain.dto.ResourceAnalysisDTO;
import com.platform.resource.domain.vo.ServiceResourceVO;
import com.platform.resource.domain.vo.ResourceAnalysisVO;

import java.util.List;

public interface ResourceAnalysisService {

    ResourceAnalysisVO analyzeResource(ResourceAnalysisDTO dto);

    List<ServiceResourceVO> listServiceResources();

    List<ServiceResourceVO> getHighConsumptionServices(Double cpuThreshold, Double memoryThreshold);

    ResourceAnalysisVO getResourceSummary();
}