package com.platform.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.ai.convert.ConversationConvert;
import com.platform.ai.domain.dto.ChatDTO;
import com.platform.ai.domain.entity.Conversation;
import com.platform.ai.domain.entity.Message;
import com.platform.ai.domain.vo.ChatVO;
import com.platform.ai.domain.vo.ConversationListVO;
import com.platform.ai.domain.vo.ConversationVO;
import com.platform.ai.domain.vo.MessageVO;
import com.platform.ai.mapper.ConversationMapper;
import com.platform.ai.mapper.MessageMapper;
import com.platform.ai.service.ConversationService;
import com.platform.common.ai.service.AiService;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final int CONTEXT_WINDOW_SIZE = 20;

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ConversationConvert conversationConvert;
    private final AiService aiService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatVO chat(Long userId, ChatDTO chatDTO) {
        Conversation conversation;

        // 获取或创建对话
        if (chatDTO.getConversationId() != null) {
            conversation = conversationMapper.selectById(chatDTO.getConversationId());
            if (conversation == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "对话不存在");
            }
            if (!conversation.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此对话");
            }
        } else {
            // 创建新对话
            conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setTitle(generateTitle(chatDTO.getMessage()));
            conversation.setModel(chatDTO.getModel() != null ? chatDTO.getModel() : "mimo");
            conversation.setStatus(1);
            conversationMapper.insert(conversation);
        }

        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setConversationId(conversation.getId());
        userMessage.setRole("user");
        userMessage.setContent(chatDTO.getMessage());
        userMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 获取历史消息作为上下文（限制窗口大小）
        String context = buildContext(conversation.getId());

        // 获取模型名称（优先使用请求中的模型，其次使用对话的模型）
        String modelName = chatDTO.getModel() != null ? chatDTO.getModel() : conversation.getModel();

        // 调用AI服务
        String reply = aiService.chat(chatDTO.getMessage(), context, modelName);

        // 保存AI回复
        Message assistantMessage = new Message();
        assistantMessage.setConversationId(conversation.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(reply);
        assistantMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(assistantMessage);

        // 构建响应
        ChatVO chatVO = new ChatVO();
        chatVO.setConversationId(conversation.getId());
        chatVO.setReply(reply);
        return chatVO;
    }

    @Override
    public Flux<String> streamChat(Long userId, ChatDTO chatDTO) {
        // 准备对话（同步部分）
        Conversation conversation = prepareConversation(userId, chatDTO);

        // 保存用户消息
        saveMessage(conversation.getId(), "user", chatDTO.getMessage());

        // 获取历史消息作为上下文
        String context = buildContext(conversation.getId());

        // 获取模型名称
        String modelName = chatDTO.getModel() != null ? chatDTO.getModel() : conversation.getModel();

        // 第一个事件返回 conversationId
        String conversationIdEvent = "conversationId:" + conversation.getId();

        // 流式调用AI服务，完成后保存回复
        StringBuilder fullReply = new StringBuilder();
        Flux<String> stream = aiService.streamChat(chatDTO.getMessage(), context, modelName)
                .doOnNext(fullReply::append)
                .doOnComplete(() -> {
                    // 流式完成后保存AI回复
                    saveMessage(conversation.getId(), "assistant", fullReply.toString());
                })
                .doOnError(e -> log.error("流式聊天失败", e));

        // 在流前面加上 conversationId 事件
        return Flux.concat(Flux.just(conversationIdEvent), stream);
    }

    private Conversation prepareConversation(Long userId, ChatDTO chatDTO) {
        if (chatDTO.getConversationId() != null) {
            Conversation conversation = conversationMapper.selectById(chatDTO.getConversationId());
            if (conversation == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "对话不存在");
            }
            if (!conversation.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此对话");
            }
            return conversation;
        } else {
            Conversation conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setTitle(generateTitle(chatDTO.getMessage()));
            conversation.setModel(chatDTO.getModel() != null ? chatDTO.getModel() : "mimo");
            conversation.setStatus(1);
            conversationMapper.insert(conversation);
            return conversation;
        }
    }

    private void saveMessage(Long conversationId, String role, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private String buildContext(Long conversationId) {
        // 获取所有消息，然后取最近 N 条
        List<Message> allMessages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreateTime)
        );

        // 限制上下文窗口：保留最近 CONTEXT_WINDOW_SIZE 条消息
        List<Message> contextMessages = allMessages.size() > CONTEXT_WINDOW_SIZE
                ? allMessages.subList(allMessages.size() - CONTEXT_WINDOW_SIZE, allMessages.size())
                : allMessages;

        StringBuilder context = new StringBuilder();
        for (Message msg : contextMessages) {
            context.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        return context.toString();
    }

    @Override
    public PageResult<ConversationListVO> listConversations(Long userId, Integer current, Integer size) {
        Page<Conversation> page = new Page<>(current, size);
        Page<Conversation> result = conversationMapper.selectPage(page,
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getStatus, 1)
                        .orderByDesc(Conversation::getUpdateTime)
        );

        List<ConversationListVO> records = conversationConvert.entityListToListVOList(result.getRecords());

        // 填充最后一条消息
        records.forEach(vo -> {
            Message lastMessage = messageMapper.selectOne(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getConversationId, vo.getId())
                            .orderByDesc(Message::getCreateTime)
                            .last("LIMIT 1")
            );
            if (lastMessage != null) {
                vo.setLastMessage(lastMessage.getContent().length() > 100
                        ? lastMessage.getContent().substring(0, 100) + "..."
                        : lastMessage.getContent());
            }
        });

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ConversationVO getConversation(Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "对话不存在");
        }

        ConversationVO vo = conversationConvert.entityToVO(conversation);

        // 获取消息列表
        List<Message> messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreateTime)
        );
        vo.setMessages(conversationConvert.messageEntityListToVOList(messages));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameConversation(Long conversationId, String title) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "对话不存在");
        }
        conversation.setTitle(title);
        conversationMapper.updateById(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "对话不存在");
        }

        // 删除消息
        messageMapper.delete(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
        );

        // 删除对话
        conversationMapper.deleteById(conversationId);
    }

    /**
     * 根据第一条消息生成对话标题
     */
    private String generateTitle(String message) {
        if (message.length() <= 50) {
            return message;
        }
        return message.substring(0, 50) + "...";
    }
}
