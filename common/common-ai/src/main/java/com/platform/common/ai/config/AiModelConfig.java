package com.platform.common.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * AI模型配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.models")
public class AiModelConfig {

    /**
     * 默认模型名称
     */
    private String defaultModel = "mimo";

    /**
     * 模型配置映射
     */
    private Map<String, ModelProperties> config = new HashMap<>();

    /**
     * 模型属性
     */
    @Data
    public static class ModelProperties {
        /**
         * API 基础URL
         */
        private String baseUrl;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * 模型名称
         */
        private String model;
    }
}
