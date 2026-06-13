package com.platform.blog.service;

import com.platform.blog.domain.dto.TagCreateDTO;
import com.platform.blog.domain.dto.TagUpdateDTO;
import com.platform.blog.domain.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 获取所有标签列表
     *
     * @return 标签列表
     */
    List<TagVO> listAll();

    /**
     * 获取标签详情
     *
     * @param id 标签ID
     * @return 标签详情
     */
    TagVO getById(Long id);

    /**
     * 创建标签
     *
     * @param createDTO 创建标签DTO
     * @return 标签ID
     */
    Long create(TagCreateDTO createDTO);

    /**
     * 更新标签
     *
     * @param id 标签ID
     * @param updateDTO 更新标签DTO
     */
    void update(Long id, TagUpdateDTO updateDTO);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void delete(Long id);
}
