package com.platform.common.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatModel 工厂
 * 支持多模型动态创建
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatModelFactory {

    private final AiModelConfig aiModelConfig;

    /**
     * ChatModel 缓存
     */
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();

    /**
     * 获取 ChatModel
     *
     * @param modelName 模型名称，为 null 时使用默认模型
     * @return ChatModel
     */
    public ChatModel getChatModel(String modelName) {
        String model = (modelName != null && !modelName.isEmpty()) ? modelName : aiModelConfig.getDefaultModel();
        return chatModelCache.computeIfAbsent(model, this::createChatModel);
    }

    /**
     * 获取默认 ChatModel
     *
     * @return ChatModel
     */
    public ChatModel getDefaultChatModel() {
        return getChatModel(aiModelConfig.getDefaultModel());
    }

    /**
     * 创建 ChatModel
     */
    private ChatModel createChatModel(String modelName) {
        AiModelConfig.ModelProperties properties = aiModelConfig.getConfig().get(modelName);
        if (properties == null) {
            throw new IllegalArgumentException("未找到模型配置: " + modelName);
        }

        log.info("创建 ChatModel: {}, baseUrl: {}, model: {}", modelName, properties.getBaseUrl(), properties.getModel());

        // 创建 OpenAI 兼容的 API
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .build();

        // 创建 ChatModel
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(properties.getModel())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }
}
