package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.entity.Favorite;
import com.platform.blog.mapper.ArticleMapper;
import com.platform.blog.mapper.FavoriteMapper;
import com.platform.blog.service.FavoriteService;
import com.platform.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long articleId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查是否已收藏
        if (isFavorited(articleId)) {
            return;
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        favoriteMapper.insert(favorite);

        // 更新文章收藏数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setFavoriteCount(article.getFavoriteCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(Long articleId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 删除收藏记录
        int deleted = favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getArticleId, articleId)
        );

        if (deleted > 0) {
            // 更新文章收藏数
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getFavoriteCount() > 0) {
                article.setFavoriteCount(article.getFavoriteCount() - 1);
                articleMapper.updateById(article);
            }
        }
    }

    @Override
    public boolean isFavorited(Long articleId) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            return false;
        }

        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getArticleId, articleId)
        );
        return count > 0;
    }
}
