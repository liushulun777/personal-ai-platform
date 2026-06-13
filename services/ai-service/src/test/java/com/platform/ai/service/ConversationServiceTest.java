package com.platform.ai.service;

import com.platform.ai.convert.ConversationConvert;
import com.platform.ai.domain.dto.ChatDTO;
import com.platform.ai.domain.entity.Conversation;
import com.platform.ai.domain.entity.Message;
import com.platform.ai.domain.vo.ChatVO;
import com.platform.ai.domain.vo.ConversationVO;
import com.platform.ai.mapper.ConversationMapper;
import com.platform.ai.mapper.MessageMapper;
import com.platform.ai.service.impl.ConversationServiceImpl;
import com.platform.common.ai.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 对话服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("对话服务测试")
class ConversationServiceTest {

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ConversationConvert conversationConvert;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private Long testUserId;
    private Conversation testConversation;
    private ChatDTO testChatDTO;

    @BeforeEach
    void setUp() {
        testUserId = 1L;

        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setUserId(testUserId);
        testConversation.setTitle("测试对话");
        testConversation.setModel("mimo");
        testConversation.setStatus(1);
        testConversation.setCreateTime(LocalDateTime.now());
        testConversation.setUpdateTime(LocalDateTime.now());

        testChatDTO = new ChatDTO();
        testChatDTO.setMessage("你好，请介绍一下 Spring Boot");
        testChatDTO.setModel("mimo");
    }

    @Test
    @DisplayName("创建新对话并聊天 - 成功")
    void chat_NewConversation_Success() {
        // Given
        when(conversationMapper.insert(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            conversation.setId(1L); // 模拟 MyBatis Plus 回填 ID
            return 1;
        });
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(aiService.chat(anyString(), anyString(), anyString())).thenReturn("Spring Boot 是一个...");

        // When
        ChatVO result = conversationService.chat(testUserId, testChatDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getConversationId());
        assertEquals(1L, result.getConversationId());
        assertNotNull(result.getReply());
        verify(conversationMapper, times(1)).insert(any(Conversation.class));
        verify(messageMapper, times(2)).insert(any(Message.class)); // 用户消息 + AI回复
    }

    @Test
    @DisplayName("在已有对话中聊天 - 成功")
    void chat_ExistingConversation_Success() {
        // Given
        testChatDTO.setConversationId(1L);
        when(conversationMapper.selectById(1L)).thenReturn(testConversation);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(aiService.chat(anyString(), anyString(), anyString())).thenReturn("Spring Boot 是一个...");

        // When
        ChatVO result = conversationService.chat(testUserId, testChatDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getConversationId());
        verify(conversationMapper, never()).insert(any(Conversation.class));
    }

    @Test
    @DisplayName("获取对话详情 - 成功")
    void getConversation_Success() {
        // Given
        when(conversationMapper.selectById(1L)).thenReturn(testConversation);
        when(conversationConvert.entityToVO(any(Conversation.class))).thenReturn(new ConversationVO());

        // When
        ConversationVO result = conversationService.getConversation(1L);

        // Then
        assertNotNull(result);
        verify(conversationMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("删除对话 - 成功")
    void deleteConversation_Success() {
        // Given
        when(conversationMapper.selectById(1L)).thenReturn(testConversation);
        when(messageMapper.delete(any())).thenReturn(1);
        when(conversationMapper.deleteById(1L)).thenReturn(1);

        // When
        conversationService.deleteConversation(1L);

        // Then
        verify(messageMapper, times(1)).delete(any());
        verify(conversationMapper, times(1)).deleteById(1L);
    }
}
