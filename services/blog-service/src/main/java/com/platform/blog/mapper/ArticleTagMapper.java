package com.platform.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.blog.domain.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章标签关联Mapper
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
}
