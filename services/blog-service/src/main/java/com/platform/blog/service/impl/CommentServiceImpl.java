package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.blog.domain.dto.CommentCreateDTO;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.entity.Comment;
import com.platform.blog.domain.vo.CommentVO;
import com.platform.blog.mapper.ArticleMapper;
import com.platform.blog.mapper.CommentMapper;
import com.platform.blog.service.CommentService;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CommentVO> listByArticleId(Long articleId) {
        // 查询所有评论
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, CommonConstant.STATUS_ENABLED)
                        .eq(Comment::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByDesc(Comment::getCreateTime)
        );

        // 转换为VO
        List<CommentVO> commentVOs = comments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(commentVOs, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CommentCreateDTO createDTO) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查文章是否存在
        Article article = articleMapper.selectById(createDTO.getArticleId());
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(createDTO.getArticleId());
        comment.setParentId(createDTO.getParentId() != null ? createDTO.getParentId() : 0L);
        comment.setUserId(userId);
        comment.setContent(createDTO.getContent());
        comment.setLikeCount(0);
        comment.setStatus(CommonConstant.STATUS_ENABLED);
        commentMapper.insert(comment);

        // 更新文章评论数
        article.setCommentCount(article.getCommentCount() + 1);
        articleMapper.updateById(article);

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评论不存在");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }

        commentMapper.deleteById(id);

        // 更新文章评论数
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null && article.getCommentCount() > 0) {
            article.setCommentCount(article.getCommentCount() - 1);
            articleMapper.updateById(article);
        }
    }

    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setParentId(comment.getParentId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());
        // TODO: 查询用户信息填充username, nickname, avatar
        return vo;
    }

    /**
     * 构建树形结构
     */
    private List<CommentVO> buildTree(List<CommentVO> comments, Long parentId) {
        List<CommentVO> tree = new ArrayList<>();
        for (CommentVO comment : comments) {
            if (parentId.equals(comment.getParentId())) {
                List<CommentVO> children = buildTree(comments, comment.getId());
                comment.setChildren(children.isEmpty() ? null : children);
                tree.add(comment);
            }
        }
        return tree;
    }
}
