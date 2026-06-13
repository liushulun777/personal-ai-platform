package com.platform.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.ai.domain.entity.Prompt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词模板Mapper
 */
@Mapper
public interface PromptMapper extends BaseMapper<Prompt> {
}
