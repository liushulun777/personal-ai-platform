package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.blog.convert.ArticleConvert;
import com.platform.blog.domain.dto.ArticleCreateDTO;
import com.platform.blog.domain.dto.ArticleQueryDTO;
import com.platform.blog.domain.dto.ArticleUpdateDTO;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.entity.ArticleTag;
import com.platform.blog.domain.entity.Category;
import com.platform.blog.domain.entity.Tag;
import com.platform.blog.domain.vo.ArticleDetailVO;
import com.platform.blog.domain.vo.ArticleVO;
import com.platform.blog.domain.vo.TagVO;
import com.platform.blog.event.ArticleEvent;
import com.platform.blog.event.ArticleEventPublisher;
import com.platform.blog.mapper.ArticleMapper;
import com.platform.blog.mapper.ArticleTagMapper;
import com.platform.blog.mapper.CategoryMapper;
import com.platform.blog.mapper.TagMapper;
import com.platform.blog.service.ArticleService;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleConvert articleConvert;
    private final ArticleEventPublisher articleEventPublisher;

    @Override
    public PageResult<ArticleVO> page(ArticleQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Article::getTitle, queryDTO.getTitle());
        wrapper.eq(queryDTO.getCategoryId() != null, Article::getCategoryId, queryDTO.getCategoryId());
        wrapper.eq(queryDTO.getStatus() != null, Article::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getAuthorId() != null, Article::getAuthorId, queryDTO.getAuthorId());
        wrapper.orderByDesc(Article::getIsTop);
        wrapper.orderByDesc(Article::getPublishTime);

        // 分页查询
        Page<Article> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Article> result = articleMapper.selectPage(page, wrapper);

        // 转换结果
        List<ArticleVO> records = articleConvert.entityListToVOList(result.getRecords());
        // 填充分类名称和标签
        records.forEach(this::fillArticleVO);

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ArticleDetailVO getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
        }

        // 增加浏览量
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        ArticleDetailVO detailVO = articleConvert.entityToDetailVO(article);

        // 填充分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                detailVO.setCategoryName(category.getName());
            }
        }

        // 填充标签
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, id)
        );

        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());
            detailVO.setTagIds(tagIds);

            List<Tag> tags = tagMapper.selectByIds(tagIds);
            List<TagVO> tagVOs = tags.stream()
                    .map(tag -> {
                        TagVO tagVO = new TagVO();
                        tagVO.setId(tag.getId());
                        tagVO.setName(tag.getName());
                        tagVO.setSlug(tag.getSlug());
                        tagVO.setColor(tag.getColor());
                        return tagVO;
                    })
                    .collect(Collectors.toList());
            detailVO.setTags(tagVOs);
        }

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleCreateDTO createDTO) {
        Long userId = SecurityUtils.getCurrentUserId();

        Article article = articleConvert.createDTOToEntity(createDTO);
        article.setAuthorId(userId);
        article.setStatus(0); // 草稿
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setCommentCount(0);
        article.setIsTop(createDTO.getIsTop() != null ? createDTO.getIsTop() : 0);
        article.setIsOriginal(createDTO.getIsOriginal() != null ? createDTO.getIsOriginal() : 1);
        articleMapper.insert(article);

        // 保存文章标签关联
        if (createDTO.getTagIds() != null && !createDTO.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), createDTO.getTagIds());
        }

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ArticleUpdateDTO updateDTO) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
        }

        article.setTitle(updateDTO.getTitle());
        article.setSummary(updateDTO.getSummary());
        article.setContent(updateDTO.getContent());
        article.setCover(updateDTO.getCover());
        article.setCategoryId(updateDTO.getCategoryId());
        article.setIsTop(updateDTO.getIsTop());
        article.setIsOriginal(updateDTO.getIsOriginal());
        article.setSourceUrl(updateDTO.getSourceUrl());
        articleMapper.updateById(article);

        // 更新文章标签关联
        if (updateDTO.getTagIds() != null) {
            // 删除原有标签关联
            articleTagMapper.delete(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getArticleId, id)
            );
            // 保存新的标签关联
            if (!updateDTO.getTagIds().isEmpty()) {
                saveArticleTags(id, updateDTO.getTagIds());
            }
        }

        // 如果文章已发布，发布更新事件
        if (article.getStatus() == 1) {
            ArticleEvent event = buildArticleEvent(article);
            articleEventPublisher.publishArticleUpdateEvent(event);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
        }

        // 删除文章标签关联
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, id)
        );

        // 删除文章
        articleMapper.deleteById(id);

        // 发布文章删除事件
        articleEventPublisher.publishArticleDeleteEvent(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
        }

        article.setStatus(1); // 已发布
        article.setPublishTime(LocalDateTime.now());
        articleMapper.updateById(article);

        // 发布文章发布事件
        ArticleEvent event = buildArticleEvent(article);
        articleEventPublisher.publishArticlePublishEvent(event);
    }

    @Override
    public void archive(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
        }

        article.setStatus(2); // 已归档
        articleMapper.updateById(article);
    }

    /**
     * 保存文章标签关联
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }

    /**
     * 构建文章事件
     */
    private ArticleEvent buildArticleEvent(Article article) {
        ArticleEvent event = new ArticleEvent();
        event.setArticleId(article.getId());
        event.setTitle(article.getTitle());
        event.setSummary(article.getSummary());
        event.setContent(article.getContent());
        event.setAuthorId(article.getAuthorId());
        event.setCategoryId(article.getCategoryId());
        event.setStatus(article.getStatus());
        event.setViewCount(article.getViewCount());
        event.setLikeCount(article.getLikeCount());
        event.setFavoriteCount(article.getFavoriteCount());
        event.setCommentCount(article.getCommentCount());
        event.setIsTop(article.getIsTop());
        event.setPublishTime(article.getPublishTime());
        event.setCreateTime(article.getCreateTime());
        event.setUpdateTime(article.getUpdateTime());

        // 填充分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                event.setCategoryName(category.getName());
            }
        }

        // 填充标签
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, article.getId())
        );
        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectByIds(tagIds);
            event.setTags(tags.stream().map(Tag::getName).collect(Collectors.toList()));
        }

        return event;
    }

    /**
     * 填充文章VO的分类名称和标签
     */
    private void fillArticleVO(ArticleVO articleVO) {
        // 填充分类名称
        if (articleVO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(articleVO.getCategoryId());
            if (category != null) {
                articleVO.setCategoryName(category.getName());
            }
        }

        // 填充标签
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, articleVO.getId())
        );

        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectByIds(tagIds);
            List<TagVO> tagVOs = tags.stream()
                    .map(tag -> {
                        TagVO tagVO = new TagVO();
                        tagVO.setId(tag.getId());
                        tagVO.setName(tag.getName());
                        tagVO.setSlug(tag.getSlug());
                        tagVO.setColor(tag.getColor());
                        return tagVO;
                    })
                    .collect(Collectors.toList());
            articleVO.setTags(tagVOs);
        } else {
            articleVO.setTags(Collections.emptyList());
        }
    }
}
