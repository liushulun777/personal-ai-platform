package com.platform.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.blog.domain.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 文章Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 统计所有文章总浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM biz_article WHERE deleted = 0")
    Long sumViewCount();

    /**
     * 统计所有文章总点赞数
     */
    @Select("SELECT COALESCE(SUM(like_count), 0) FROM biz_article WHERE deleted = 0")
    Long sumLikeCount();
}
