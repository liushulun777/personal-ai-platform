package com.platform.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.ai.domain.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
