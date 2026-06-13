package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.entity.Category;
import com.platform.blog.domain.entity.Comment;
import com.platform.blog.domain.entity.Tag;
import com.platform.blog.domain.vo.BlogStatisticsVO;
import com.platform.blog.mapper.ArticleMapper;
import com.platform.blog.mapper.CategoryMapper;
import com.platform.blog.mapper.CommentMapper;
import com.platform.blog.mapper.TagMapper;
import com.platform.blog.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;

    @Override
    public BlogStatisticsVO getBlogStatistics() {
        Long articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>());
        Long categoryCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>());
        Long tagCount = tagMapper.selectCount(new LambdaQueryWrapper<Tag>());
        Long commentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>());
        Long totalViews = articleMapper.sumViewCount();
        Long totalLikes = articleMapper.sumLikeCount();

        BlogStatisticsVO vo = new BlogStatisticsVO();
        vo.setArticleCount(articleCount);
        vo.setCategoryCount(categoryCount);
        vo.setTagCount(tagCount);
        vo.setCommentCount(commentCount);
        vo.setTotalViews(totalViews);
        vo.setTotalLikes(totalLikes);
        return vo;
    }
}
