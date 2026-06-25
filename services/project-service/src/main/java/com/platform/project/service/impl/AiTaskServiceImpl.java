package com.platform.project.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.ai.service.AiService;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.domain.dto.AiTaskDecomposeDTO;
import com.platform.project.domain.entity.Task;
import com.platform.project.domain.vo.AiTaskDecomposeVO;
import com.platform.project.mapper.TaskMapper;
import com.platform.project.service.AiTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl implements AiTaskService {

    private final AiService aiService;
    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiTaskDecomposeVO> decompose(AiTaskDecomposeDTO dto) {
        try {
            String prompt = buildDecomposePrompt(dto);
            String result = aiService.chat(prompt);

            // 解析AI返回的JSON
            List<AiTaskDecomposeVO> tasks = parseDecomposeResult(result);

            // 设置排序顺序
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getSortOrder() == null) {
                    tasks.get(i).setSortOrder(i + 1);
                }
            }

            log.info("AI任务拆解完成, 原始内容: {}, 拆解数量: {}", dto.getContent(), tasks.size());
            return tasks;
        } catch (Exception e) {
            log.error("AI任务拆解失败", e);
            throw new RuntimeException("AI任务拆解失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> decomposeAndCreate(AiTaskDecomposeDTO dto) {
        List<AiTaskDecomposeVO> decomposedTasks = decompose(dto);
        List<Long> taskIds = new ArrayList<>();
        Map<Integer, Long> sortOrderToIdMap = new HashMap<>();

        // 第一遍：创建所有任务
        for (AiTaskDecomposeVO decomposedTask : decomposedTasks) {
            Task task = new Task();
            task.setProjectId(dto.getProjectId());
            task.setTitle(decomposedTask.getTitle());
            task.setDescription(decomposedTask.getDescription());
            task.setPriority(decomposedTask.getPriority() != null ? decomposedTask.getPriority() : 1);
            task.setSortOrder(decomposedTask.getSortOrder());
            task.setEstimatedHours(decomposedTask.getEstimatedHours());
            task.setStatus(0); // BACKLOG
            task.setSourceType("AI_GENERATED");
            task.setReporterId(SecurityUtils.getCurrentUserIdOrNull());
            taskMapper.insert(task);
            taskIds.add(task.getId());
            sortOrderToIdMap.put(decomposedTask.getSortOrder(), task.getId());
        }

        log.info("AI任务拆解并创建完成, projectId: {}, 创建数量: {}", dto.getProjectId(), taskIds.size());
        return taskIds;
    }

    private String buildDecomposePrompt(AiTaskDecomposeDTO dto) {
        String content = dto.getContent();
        String techStack = dto.getTechStack();
        Integer maxTasks = dto.getMaxTasks();
        String granularity = dto.getGranularity();

        // 技术栈上下文
        String techContext = "";
        if (techStack != null && !techStack.isEmpty()) {
            techContext = "\n## 项目技术栈\n" + techStack + "\n";
        }

        // 任务数量限制
        String maxTasksHint = "";
        if (maxTasks != null && maxTasks > 0) {
            maxTasksHint = String.format("\n任务数量控制在 %d 个以内。\n", maxTasks);
        }

        // 粒度控制
        String granularityHint = "";
        if (granularity != null) {
            switch (granularity) {
                case "fine":
                    granularityHint = "\n任务粒度要求：每个任务应该是 1-2 天可完成的具体开发任务，拆分要细致。\n";
                    break;
                case "coarse":
                    granularityHint = "\n任务粒度要求：每个任务可以是 1-2 周的较大功能模块。\n";
                    break;
                default:
                    granularityHint = "\n任务粒度要求：每个任务应该是 3-5 天可完成的功能单元。\n";
            }
        }

        return String.format(
                "你是一个资深的软件项目经理和技术架构师。请根据以下需求，拆解出具体可执行的开发任务。\n\n" +
                "## 需求内容\n%s\n\n" +
                "%s%s%s" +
                "## 拆解要求\n" +
                "1. 任务必须是具体的、可执行的开发任务，不要拆出模糊的任务（如'完善系统'、'优化性能'）\n" +
                "2. 每个任务应该有明确的交付物（代码、配置、文档等）\n" +
                "3. 任务之间要有合理的执行顺序，通过 sortOrder 和 dependencies 体现\n" +
                "4. 前置任务（如数据库设计）应该排在前面\n" +
                "5. 并行开发的任务可以设置相同的 dependencies\n\n" +
                "## 输出格式\n" +
                "请以JSON数组格式返回，每个任务包含以下字段：\n" +
                "- title: 任务标题，具体描述要做什么（必填）\n" +
                "- description: 详细描述，包含具体要实现的内容和技术要点（必填）\n" +
                "- priority: 优先级（0-低, 1-中, 2-高, 3-紧急，默认1）\n" +
                "- sortOrder: 执行顺序，从1开始（必填）\n" +
                "- dependencies: 依赖的任务序号数组，表示需要在哪些任务完成后才能开始（必填，无依赖则为空数组[]）\n" +
                "- estimatedHours: 预估工时，单位小时（必填）\n" +
                "- taskType: 任务类型 design/development/testing/deployment（必填）\n\n" +
                "## 示例\n" +
                "[\n" +
                "  {\"title\": \"设计用户表结构\", \"description\": \"创建user表，包含id、username、password、email等字段\", \"priority\": 2, \"sortOrder\": 1, \"dependencies\": [], \"estimatedHours\": 4, \"taskType\": \"design\"},\n" +
                "  {\"title\": \"实现用户注册接口\", \"description\": \"创建UserController，实现POST /api/users/register接口\", \"priority\": 2, \"sortOrder\": 2, \"dependencies\": [1], \"estimatedHours\": 8, \"taskType\": \"development\"}\n" +
                "]\n\n" +
                "只返回JSON数组，不要添加任何其他说明。",
                content, techContext, maxTasksHint, granularityHint
        );
    }

    private List<AiTaskDecomposeVO> parseDecomposeResult(String result) {
        try {
            // 清理AI返回的内容，提取JSON部分
            String json = extractJson(result);
            return objectMapper.readValue(json, new TypeReference<List<AiTaskDecomposeVO>>() {});
        } catch (Exception e) {
            log.error("解析AI任务拆解结果失败: {}", result, e);
            throw new RuntimeException("解析AI任务拆解结果失败", e);
        }
    }

    private String extractJson(String content) {
        // 尝试提取JSON数组
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}
