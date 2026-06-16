package com.platform.ai.service;

import com.platform.ai.domain.dto.PromptCreateDTO;
import com.platform.ai.domain.vo.PromptVO;
import com.platform.common.core.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * Prompt模板服务接口
 */
public interface PromptService {

    /**
     * 分页查询Prompt模板
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 分页结果
     */
    PageResult<PromptVO> listPrompts(Integer current, Integer size);

    /**
     * 按分类查询Prompt模板
     *
     * @param category 分类
     * @return 模板列表
     */
    List<PromptVO> listByCategory(String category);

    /**
     * 获取所有分类及数量
     *
     * @return 分类统计
     */
    Map<String, Long> getCategories();

    /**
     * 创建Prompt模板
     *
     * @param dto 创建请求
     * @return 创建的模板
     */
    PromptVO createPrompt(PromptCreateDTO dto);

    /**
     * 更新Prompt模板
     *
     * @param id  模板ID
     * @param dto 更新请求
     * @return 更新后的模板
     */
    PromptVO updatePrompt(Long id, PromptCreateDTO dto);

    /**
     * 删除Prompt模板
     *
     * @param id 模板ID
     */
    void deletePrompt(Long id);
}
