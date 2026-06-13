package com.platform.ai.service;

import com.platform.ai.domain.dto.ChatDTO;
import com.platform.ai.domain.vo.ChatVO;
import com.platform.ai.domain.vo.ConversationListVO;
import com.platform.ai.domain.vo.ConversationVO;
import com.platform.common.core.result.PageResult;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 对话服务接口
 */
public interface ConversationService {

    /**
     * 聊天
     *
     * @param userId  用户ID
     * @param chatDTO 聊天请求
     * @return 聊天响应
     */
    ChatVO chat(Long userId, ChatDTO chatDTO);

    /**
     * 流式聊天
     *
     * @param userId  用户ID
     * @param chatDTO 聊天请求
     * @return 流式回复内容
     */
    Flux<String> streamChat(Long userId, ChatDTO chatDTO);

    /**
     * 获取对话列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size   每页大小
     * @return 对话列表
     */
    PageResult<ConversationListVO> listConversations(Long userId, Integer current, Integer size);

    /**
     * 获取对话详情
     *
     * @param conversationId 对话ID
     * @return 对话详情
     */
    ConversationVO getConversation(Long conversationId);

    /**
     * 删除对话
     *
     * @param conversationId 对话ID
     */
    void deleteConversation(Long conversationId);
}
