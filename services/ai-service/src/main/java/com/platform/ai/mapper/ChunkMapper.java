package com.platform.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.ai.domain.entity.Chunk;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文本分块Mapper
 */
@Mapper
public interface ChunkMapper extends BaseMapper<Chunk> {
}
