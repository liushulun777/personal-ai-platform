package com.platform.knowledge.config;

import com.platform.common.ai.config.AiModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EmbeddingModel 配置
 * 使用自定义 ai.models 配置创建 EmbeddingModel，而非依赖 Spring AI 自动配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmbeddingModelConfig {

    private final AiModelConfig aiModelConfig;

    @Bean
    public EmbeddingModel embeddingModel() {
        // 使用嵌入专用模型配置（未配置则回退到默认模型）
        AiModelConfig.ModelProperties properties = aiModelConfig.getEmbeddingModelProperties();
        if (properties == null) {
            throw new IllegalArgumentException("未找到嵌入模型配置, embeddingProvider: " + aiModelConfig.getEmbeddingProvider());
        }

        log.info("创建 EmbeddingModel, baseUrl: {}, model: {}", properties.getBaseUrl(), properties.getModel());

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .build();

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(properties.getModel())
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }
}
