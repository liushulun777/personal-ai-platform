package com.platform.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.ai.domain.entity.Document;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
