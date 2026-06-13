package com.platform.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.blog.domain.entity.Like;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞Mapper
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {
}
