package com.platform.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.blog.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
