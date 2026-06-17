package com.platform.project.service;

import com.platform.common.core.result.PageResult;
import com.platform.project.domain.dto.ProjectCreateDTO;
import com.platform.project.domain.dto.ProjectQueryDTO;
import com.platform.project.domain.dto.ProjectUpdateDTO;
import com.platform.project.domain.vo.ProjectVO;

import java.util.List;
import java.util.Map;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 分页查询项目
     */
    PageResult<ProjectVO> page(ProjectQueryDTO queryDTO);

    /**
     * 获取项目详情
     */
    ProjectVO getById(Long id);

    /**
     * 创建项目
     */
    Long create(ProjectCreateDTO dto);

    /**
     * 更新项目
     */
    void update(ProjectUpdateDTO dto);

    /**
     * 删除项目
     */
    void delete(Long id);

    /**
     * AI 拆分任务
     *
     * @param projectId 项目ID
     * @param content   需求描述
     * @return 创建的任务ID列表
     */
    List<Long> aiDecomposeTasks(Long projectId, String content);

    /**
     * AI 拆分任务（带更多参数）
     */
    List<Long> aiDecomposeTasks(Long projectId, String content, String techStack, Integer maxTasks, String granularity);

    /**
     * 发布项目并触发 Agent 执行
     *
     * @param projectId   项目ID
     * @param requirement 需求描述
     * @param techStack   技术栈
     * @return 执行结果
     */
    Map<String, Object> publishProject(Long projectId, String requirement, String techStack);
}
