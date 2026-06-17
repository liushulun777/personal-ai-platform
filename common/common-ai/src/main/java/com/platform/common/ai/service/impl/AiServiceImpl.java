package com.platform.common.ai.service.impl;

import com.platform.common.ai.config.ChatModelFactory;
import com.platform.common.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * AI服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatModelFactory chatModelFactory;

    @Override
    public String chat(String prompt) {
        return chat(prompt, null, null);
    }

    @Override
    public String chat(String prompt, String context) {
        return chat(prompt, context, null);
    }

    @Override
    public String chat(String prompt, String context, String modelName) {
        try {
            List<Message> messages = buildMessages(prompt, context);

            // 获取对应的 ChatModel
            ChatModel chatModel = chatModelFactory.getChatModel(modelName);

            // 调用AI模型
            Prompt chatPrompt = new Prompt(messages);
            var chatResponse = chatModel.call(chatPrompt);
            return chatResponse.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("AI调用失败, model: {}", modelName, e);
            throw new RuntimeException("AI调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> streamChat(String prompt, String context, String modelName) {
        try {
            List<org.springframework.ai.chat.messages.Message> messages = buildMessages(prompt, context);

            // 获取对应的 ChatModel
            ChatModel chatModel = chatModelFactory.getChatModel(modelName);

            // 流式调用AI模型
            Prompt chatPrompt = new Prompt(messages);
            return chatModel.stream(chatPrompt)
                    .map(response -> {
                        var result = response.getResult();
                        if (result != null && result.getOutput() != null) {
                            String text = result.getOutput().getText();
                            return text != null ? text : "";
                        }
                        return "";
                    })
                    .filter(text -> !text.isEmpty());
        } catch (Exception e) {
            log.error("AI流式调用失败, model: {}", modelName, e);
            return Flux.error(new RuntimeException("AI流式调用失败: " + e.getMessage(), e));
        }
    }

    private List<org.springframework.ai.chat.messages.Message> buildMessages(String prompt, String context) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();

        // 添加系统消息
        if (context != null && !context.isEmpty()) {
            messages.add(new SystemMessage("你是一个 helpful AI 助手。以下是对话历史：\n" + context));
        } else {
            messages.add(new SystemMessage("你是一个 helpful AI 助手。"));
        }

        // 添加用户消息
        messages.add(new UserMessage(prompt));
        return messages;
    }

    @Override
    public String generateSummary(String content) {
        String prompt = String.format(
                "请为以下文章生成一个简洁的摘要，摘要长度不超过200个字符。只返回摘要内容，不要添加任何其他说明。\n\n文章内容：\n%s",
                content
        );
        return chat(prompt);
    }

    @Override
    public List<String> generateTags(String title, String content) {
        String prompt = String.format(
                "请为以下文章生成5个相关的标签。标签应该是简短的关键词，用逗号分隔。只返回标签列表，不要添加任何其他说明。\n\n标题：%s\n\n内容：\n%s",
                title, content
        );
        String result = chat(prompt);
        return List.of(result.split("[,，、]"));
    }

    @Override
    public List<String> generateTitles(String content) {
        String prompt = String.format(
                "请为以下文章生成5个吸引人的标题建议。每个标题占一行。只返回标题列表，不要添加任何其他说明。\n\n文章内容：\n%s",
                content
        );
        String result = chat(prompt);
        return List.of(result.split("\n"));
    }
}
