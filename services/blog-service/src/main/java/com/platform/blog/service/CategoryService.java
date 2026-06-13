package com.platform.blog.service;

import com.platform.blog.domain.dto.CategoryCreateDTO;
import com.platform.blog.domain.dto.CategoryUpdateDTO;
import com.platform.blog.domain.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<CategoryVO> listAll();

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryVO getById(Long id);

    /**
     * 创建分类
     *
     * @param createDTO 创建分类DTO
     * @return 分类ID
     */
    Long create(CategoryCreateDTO createDTO);

    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param updateDTO 更新分类DTO
     */
    void update(Long id, CategoryUpdateDTO updateDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void delete(Long id);
}
