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

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

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

        // 获取历史消息作为上下文
        List<Message> historyMessages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversation.getId())
                        .orderByAsc(Message::getCreateTime)
        );

        // 构建上下文
        StringBuilder context = new StringBuilder();
        for (Message msg : historyMessages) {
            context.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }

        // 获取模型名称（优先使用请求中的模型，其次使用对话的模型）
        String modelName = chatDTO.getModel() != null ? chatDTO.getModel() : conversation.getModel();

        // 调用AI服务
        String reply = aiService.chat(chatDTO.getMessage(), context.toString(), modelName);

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
