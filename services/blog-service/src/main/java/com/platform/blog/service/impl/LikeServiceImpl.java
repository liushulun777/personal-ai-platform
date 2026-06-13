package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.entity.Comment;
import com.platform.blog.domain.entity.Like;
import com.platform.blog.mapper.ArticleMapper;
import com.platform.blog.mapper.CommentMapper;
import com.platform.blog.mapper.LikeMapper;
import com.platform.blog.service.LikeService;
import com.platform.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long targetId, Integer targetType) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查是否已点赞
        if (isLiked(targetId, targetType)) {
            return;
        }

        // 创建点赞记录
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setTargetType(targetType);
        likeMapper.insert(like);

        // 更新计数
        if (targetType == 1) {
            // 文章点赞
            Article article = articleMapper.selectById(targetId);
            if (article != null) {
                article.setLikeCount(article.getLikeCount() + 1);
                articleMapper.updateById(article);
            }
        } else if (targetType == 2) {
            // 评论点赞
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null) {
                comment.setLikeCount(comment.getLikeCount() + 1);
                commentMapper.updateById(comment);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long targetId, Integer targetType) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 删除点赞记录
        int deleted = likeMapper.delete(
                new LambdaQueryWrapper<Like>()
                        .eq(Like::getUserId, userId)
                        .eq(Like::getTargetId, targetId)
                        .eq(Like::getTargetType, targetType)
        );

        if (deleted > 0) {
            // 更新计数
            if (targetType == 1) {
                Article article = articleMapper.selectById(targetId);
                if (article != null && article.getLikeCount() > 0) {
                    article.setLikeCount(article.getLikeCount() - 1);
                    articleMapper.updateById(article);
                }
            } else if (targetType == 2) {
                Comment comment = commentMapper.selectById(targetId);
                if (comment != null && comment.getLikeCount() > 0) {
                    comment.setLikeCount(comment.getLikeCount() - 1);
                    commentMapper.updateById(comment);
                }
            }
        }
    }

    @Override
    public boolean isLiked(Long targetId, Integer targetType) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            return false;
        }

        Long count = likeMapper.selectCount(
                new LambdaQueryWrapper<Like>()
                        .eq(Like::getUserId, userId)
                        .eq(Like::getTargetId, targetId)
                        .eq(Like::getTargetType, targetType)
        );
        return count > 0;
    }
}
