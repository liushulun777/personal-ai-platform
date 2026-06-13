package com.platform.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.blog.domain.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
