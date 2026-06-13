package com.platform.blog.service;

import com.platform.blog.domain.dto.ArticleCreateDTO;
import com.platform.blog.domain.dto.ArticleQueryDTO;
import com.platform.blog.domain.dto.ArticleUpdateDTO;
import com.platform.blog.domain.vo.ArticleDetailVO;
import com.platform.blog.domain.vo.ArticleVO;
import com.platform.common.core.result.PageResult;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 分页查询文章
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ArticleVO> page(ArticleQueryDTO queryDTO);

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleDetailVO getById(Long id);

    /**
     * 创建文章（草稿）
     *
     * @param createDTO 创建文章DTO
     * @return 文章ID
     */
    Long create(ArticleCreateDTO createDTO);

    /**
     * 更新文章
     *
     * @param id 文章ID
     * @param updateDTO 更新文章DTO
     */
    void update(Long id, ArticleUpdateDTO updateDTO);

    /**
     * 删除文章
     *
     * @param id 文章ID
     */
    void delete(Long id);

    /**
     * 发布文章
     *
     * @param id 文章ID
     */
    void publish(Long id);

    /**
     * 归档文章
     *
     * @param id 文章ID
     */
    void archive(Long id);
}
