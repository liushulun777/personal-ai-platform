package com.platform.ai.controller;

import com.platform.common.ai.config.AiModelConfig;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型管理控制器
 */
@Tag(name = "模型管理", description = "AI模型相关接口")
@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final AiModelConfig aiModelConfig;

    /**
     * 获取可用模型列表
     */
    @Operation(summary = "获取可用模型列表", description = "获取所有可用的AI模型")
    @GetMapping
    public Result<List<Map<String, String>>> listModels() {
        List<Map<String, String>> models = aiModelConfig.getConfig().entrySet().stream()
                .map(entry -> {
                    Map<String, String> model = new java.util.HashMap<>();
                    model.put("name", entry.getKey());
                    model.put("model", entry.getValue().getModel());
                    model.put("isDefault", entry.getKey().equals(aiModelConfig.getDefaultModel()) ? "true" : "false");
                    return model;
                })
                .collect(Collectors.toList());
        return Result.success(models);
    }

    /**
     * 获取默认模型
     */
    @Operation(summary = "获取默认模型", description = "获取默认的AI模型名称")
    @GetMapping("/default")
    public Result<Map<String, String>> getDefaultModel() {
        Map<String, String> result = new java.util.HashMap<>();
        result.put("defaultModel", aiModelConfig.getDefaultModel());
        return Result.success(result);
    }
}
