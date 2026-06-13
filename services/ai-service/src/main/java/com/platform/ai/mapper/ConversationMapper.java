package com.platform.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.ai.domain.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话Mapper
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
