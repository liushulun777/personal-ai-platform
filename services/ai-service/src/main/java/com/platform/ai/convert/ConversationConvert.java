package com.platform.ai.convert;

import com.platform.ai.domain.entity.Conversation;
import com.platform.ai.domain.entity.Message;
import com.platform.ai.domain.vo.ConversationListVO;
import com.platform.ai.domain.vo.ConversationVO;
import com.platform.ai.domain.vo.MessageVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 对话转换器
 */
@Mapper(componentModel = "spring")
public interface ConversationConvert {

    /**
     * 实体转VO
     *
     * @param conversation 对话实体
     * @return 对话VO
     */
    ConversationVO entityToVO(Conversation conversation);

    /**
     * 实体列表转列表VO
     *
     * @param conversations 对话实体列表
     * @return 对话列表VO
     */
    List<ConversationListVO> entityListToListVOList(List<Conversation> conversations);

    /**
     * 消息实体转VO
     *
     * @param message 消息实体
     * @return 消息VO
     */
    MessageVO messageEntityToVO(Message message);

    /**
     * 消息实体列表转VO列表
     *
     * @param messages 消息实体列表
     * @return 消息VO列表
     */
    List<MessageVO> messageEntityListToVOList(List<Message> messages);
}
