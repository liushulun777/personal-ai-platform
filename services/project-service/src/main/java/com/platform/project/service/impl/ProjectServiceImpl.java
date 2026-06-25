package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.convert.ProjectConvert;
import com.platform.project.domain.dto.AiTaskDecomposeDTO;
import com.platform.project.domain.dto.ProjectCreateDTO;
import com.platform.project.domain.dto.ProjectQueryDTO;
import com.platform.project.domain.dto.ProjectUpdateDTO;
import com.platform.project.domain.entity.Project;
import com.platform.project.domain.entity.Task;
import com.platform.project.domain.event.TaskCreatedEvent;
import com.platform.project.domain.vo.ProjectVO;
import com.platform.project.mapper.ProjectMapper;
import com.platform.project.mapper.TaskMapper;
import com.platform.project.service.AiTaskService;
import com.platform.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;
    private final ProjectConvert projectConvert;
    private final AiTaskService aiTaskService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public PageResult<ProjectVO> page(ProjectQueryDTO queryDTO) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), Project::getName, queryDTO.getName());
        wrapper.eq(queryDTO.getStatus() != null, Project::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getPriority() != null, Project::getPriority, queryDTO.getPriority());
        wrapper.eq(queryDTO.getOwnerId() != null, Project::getOwnerId, queryDTO.getOwnerId());
        wrapper.orderByDesc(Project::getCreateTime);

        Page<Project> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Project> result = projectMapper.selectPage(page, wrapper);

        List<ProjectVO> records = projectConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        return projectConvert.entityToVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectCreateDTO dto) {
        Project project = projectConvert.createDTOToEntity(dto);
        if (project.getStatus() == null) {
            project.setStatus(0);
        }
        if (project.getPriority() == null) {
            project.setPriority(1);
        }
        projectMapper.insert(project);
        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectUpdateDTO dto) {
        Project project = projectMapper.selectById(dto.getId());
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        projectConvert.updateEntityFromDTO(dto, project);
        projectMapper.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        projectMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> aiDecomposeTasks(Long projectId, String content) {
        return aiDecomposeTasks(projectId, content, null, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> aiDecomposeTasks(Long projectId, String content, String techStack, Integer maxTasks, String granularity) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        // 调用 AI 任务拆解服务
        AiTaskDecomposeDTO dto = new AiTaskDecomposeDTO();
        dto.setProjectId(projectId);
        dto.setContent(content);
        dto.setTechStack(techStack);
        dto.setMaxTasks(maxTasks);
        dto.setGranularity(granularity);

        List<Long> taskIds = aiTaskService.decomposeAndCreate(dto);

        // 更新项目状态为进行中 (1)
        project.setStatus(1);
        projectMapper.updateById(project);

        log.info("AI 拆分任务完成, projectId: {}, taskCount: {}", projectId, taskIds.size());
        return taskIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publishProject(Long projectId, String requirement, String techStack) {
        // 1. 验证项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        // 2. 构建需求内容
        String content = buildRequirementContent(project, requirement);

        // 3. AI 拆分任务
        List<Long> taskIds = aiDecomposeTasks(projectId, content, techStack, 10, "medium");

        // 4. 获取任务列表并按顺序发送 Kafka 事件
        List<Task> tasks = taskMapper.selectBatchIds(taskIds);

        // 按 sortOrder 排序
        tasks.sort(Comparator.comparingInt(t -> t.getSortOrder() != null ? t.getSortOrder() : Integer.MAX_VALUE));

        // 5. 更新项目状态为已发布
        project.setStatus(1);
        projectMapper.updateById(project);

        // 6. 发送 Kafka 事件触发 Agent 执行
        int eventCount = 0;
        for (Task task : tasks) {
            try {
                TaskCreatedEvent event = new TaskCreatedEvent();
                event.setTaskId(task.getId());
                event.setProjectId(projectId);
                event.setTitle(task.getTitle());
                event.setDescription(task.getDescription());
                // 获取当前请求的 Token
                try {
                    event.setToken(cn.dev33.satoken.stp.StpUtil.getTokenValue());
                } catch (Exception e) {
                    log.debug("获取 Token 失败: {}", e.getMessage());
                }

                kafkaTemplate.send("task-created", event);
                eventCount++;
                log.info("发送任务创建事件, taskId: {}, title: {}", task.getId(), task.getTitle());
            } catch (Exception e) {
                log.error("发送任务事件失败, taskId: {}", task.getId(), e);
            }
        }

        // 7. 构建返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("projectName", project.getName());
        result.put("taskCount", taskIds.size());
        result.put("eventCount", eventCount);
        result.put("taskIds", taskIds);
        result.put("status", "published");
        result.put("message", "项目已发布，" + taskIds.size() + " 个任务将按顺序执行");

        log.info("项目发布完成, projectId: {}, taskCount: {}, eventCount: {}", projectId, taskIds.size(), eventCount);
        return result;
    }

    /**
     * 构建需求内容
     */
    private String buildRequirementContent(Project project, String requirement) {
        StringBuilder content = new StringBuilder();

        content.append("# 项目信息\n");
        content.append("- 项目名称: ").append(project.getName()).append("\n");
        if (project.getDescription() != null) {
            content.append("- 项目描述: ").append(project.getDescription()).append("\n");
        }
        content.append("\n");

        if (requirement != null && !requirement.isEmpty()) {
            content.append("# 需求描述\n");
            content.append(requirement).append("\n");
        }

        return content.toString();
    }
}
