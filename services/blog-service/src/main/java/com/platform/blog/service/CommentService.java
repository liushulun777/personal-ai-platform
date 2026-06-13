package com.platform.blog.service;

import com.platform.blog.domain.dto.CommentCreateDTO;
import com.platform.blog.domain.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @return 评论列表（树形结构）
     */
    List<CommentVO> listByArticleId(Long articleId);

    /**
     * 创建评论
     *
     * @param createDTO 创建评论DTO
     * @return 评论ID
     */
    Long create(CommentCreateDTO createDTO);

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    void delete(Long id);
}
