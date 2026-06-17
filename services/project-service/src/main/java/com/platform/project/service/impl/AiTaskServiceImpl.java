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
import com.platform.project.service.TaskExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
            String prompt = buildDecomposePrompt(dto.getContent());
            String result = aiService.chat(prompt);

            // 解析AI返回的JSON
            List<AiTaskDecomposeVO> tasks = parseDecomposeResult(result);
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

        for (AiTaskDecomposeVO decomposedTask : decomposedTasks) {
            Task task = new Task();
            task.setProjectId(dto.getProjectId());
            task.setTitle(decomposedTask.getTitle());
            task.setDescription(decomposedTask.getDescription());
            task.setPriority(decomposedTask.getPriority() != null ? decomposedTask.getPriority() : 1);
            task.setStatus(0); // BACKLOG
            task.setSourceType("AI_GENERATED");
            task.setReporterId(SecurityUtils.getCurrentUserIdOrNull());
            taskMapper.insert(task);
            taskIds.add(task.getId());
        }

        log.info("AI任务拆解并创建完成, projectId: {}, 创建数量: {}", dto.getProjectId(), taskIds.size());
        return taskIds;
    }

    private String buildDecomposePrompt(String content) {
        return String.format(
                "你是一个项目管理专家。请将以下需求拆解为多个可执行的子任务。\n\n" +
                "需求内容：\n%s\n\n" +
                "请以JSON数组格式返回，每个任务包含以下字段：\n" +
                "- title: 任务标题（必填）\n" +
                "- priority: 优先级（0-低, 1-中, 2-高, 3-紧急，默认1）\n" +
                "- description: 任务描述（可选）\n\n" +
                "只返回JSON数组，不要添加任何其他说明。\n" +
                "示例格式：\n" +
                "[{\"title\": \"任务1\", \"priority\": 2, \"description\": \"描述1\"}, {\"title\": \"任务2\", \"priority\": 1}]",
                content
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
