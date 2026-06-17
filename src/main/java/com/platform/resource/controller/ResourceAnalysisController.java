package com.platform.resource.controller;

import com.platform.common.domain.Result;
import com.platform.resource.domain.dto.ResourceAnalysisDTO;
import com.platform.resource.domain.vo.ServiceResourceVO;
import com.platform.resource.domain.vo.ResourceAnalysisVO;
import com.platform.resource.service.ResourceAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceAnalysisController {

    private final ResourceAnalysisService resourceAnalysisService;

    @PostMapping("/analyze")
    public Result<ResourceAnalysisVO> analyzeResource(@Validated @RequestBody ResourceAnalysisDTO dto) {
        return Result.success(resourceAnalysisService.analyzeResource(dto));
    }

    @GetMapping("/list")
    public Result<List<ServiceResourceVO>> listServiceResources() {
        return Result.success(resourceAnalysisService.listServiceResources());
    }

    @GetMapping("/high-consumption")
    public Result<List<ServiceResourceVO>> getHighConsumptionServices(
            @RequestParam(defaultValue = "80") Double cpuThreshold,
            @RequestParam(defaultValue = "80") Double memoryThreshold) {
        return Result.success(resourceAnalysisService.getHighConsumptionServices(cpuThreshold, memoryThreshold));
    }

    @GetMapping("/summary")
    public Result<ResourceAnalysisVO> getResourceSummary() {
        return Result.success(resourceAnalysisService.getResourceSummary());
    }
}