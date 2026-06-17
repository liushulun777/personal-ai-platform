package com.platform.agent.service.impl;

import com.platform.agent.client.ProjectServiceClient;
import com.platform.agent.service.AgentService;
import com.platform.agent.service.CodeGenerationService;
import com.platform.agent.service.McpToolService;
import com.platform.agent.service.WorkspaceService;
import com.platform.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Agent 执行服务实现
 *
 * 执行流程：
 * 1. 创建工作区（Git Clone）
 * 2. 调用知识库获取上下文
 * 3. 调用 LLM 生成代码
 * 4. 写入文件
 * 5. 编译
 * 6. 测试
 * 7. Git Commit
 * 8. 更新任务状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final WorkspaceService workspaceService;
    private final CodeGenerationService codeGenerationService;
    private final McpToolService mcpToolService;
    private final ProjectServiceClient projectServiceClient;

    @Override
    public void executeTask(Long taskId) {
        log.info("开始执行任务, taskId: {}", taskId);
        long startTime = System.currentTimeMillis();

        String workspacePath = null;

        try {
            // 1. 更新任务状态为 DOING (2)
            projectServiceClient.startTask(taskId);
            log.info("任务状态更新为 DOING");

            // 2. 创建工作区
            workspacePath = workspaceService.createWorkspace(taskId);
            log.info("工作区创建完成: {}", workspacePath);

            // 3. 调用知识库获取上下文
            String context = mcpToolService.knowledgeSearch(
                    "Java Spring Boot 项目结构、Controller、Service、Mapper 代码示例");
            log.info("知识库检索完成, contextLength: {}", context.length());

            // 4. 调用 LLM 生成代码
            Map<String, String> codeFiles = codeGenerationService.generateCode(context, getTaskDescription(taskId));
            log.info("代码生成完成, fileCount: {}", codeFiles.size());

            // 5. 写入文件
            for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
                workspaceService.writeFile(workspacePath, entry.getKey(), entry.getValue());
            }
            log.info("文件写入完成");

            // 6. 编译
            boolean compileResult = workspaceService.compile(workspacePath);
            if (!compileResult) {
                throw new RuntimeException("编译失败");
            }

            // 7. 测试（可选）
            // boolean testResult = workspaceService.test(workspacePath);

            // 8. Git Commit
            workspaceService.commit(workspacePath, "feat: AI auto-generated code for task " + taskId);
            log.info("Git 提交完成");

            // 9. 更新任务状态为 DONE (4)
            projectServiceClient.completeTask(taskId);
            log.info("任务状态更新为 DONE");

            long duration = System.currentTimeMillis() - startTime;
            log.info("任务执行完成, taskId: {}, duration: {}ms", taskId, duration);

        } catch (Exception e) {
            log.error("任务执行失败, taskId: {}", taskId, e);

            // 更新任务状态为 BLOCKED (5)
            try {
                projectServiceClient.blockTask(taskId, "Agent 执行失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("更新任务状态失败", ex);
            }

        } finally {
            // 清理工作区
            if (workspacePath != null) {
                // workspaceService.cleanup(taskId); // 调试时保留工作区
            }
        }
    }

    /**
     * 获取任务描述
     */
    private String getTaskDescription(Long taskId) {
        try {
            Result<Object> result = projectServiceClient.getTask(taskId);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData().toString();
            }
            return "实现任务 " + taskId;
        } catch (Exception e) {
            log.warn("获取任务详情失败", e);
            return "实现任务 " + taskId;
        }
    }
}
