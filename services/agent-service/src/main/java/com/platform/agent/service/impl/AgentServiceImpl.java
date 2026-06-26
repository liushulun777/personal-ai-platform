package com.platform.agent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.platform.agent.client.ProjectServiceClient;
import com.platform.agent.config.AgentConfig;
import com.platform.agent.config.FeignAuthConfig;
import com.platform.agent.domain.model.TaskExecutionResult;
import com.platform.agent.service.*;
import com.platform.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Agent 执行服务实现
 *
 * 核心改进：
 * 1. 任务连贯性 - 执行时读取前序任务产出作为上下文
 * 2. 自检机制 - 静态分析 + 单元测试 + 代码审查
 * 3. 动态配置 - 超时时间和迭代次数可配置
 * 4. 防重复执行 - 检查任务是否正在执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final WorkspaceService workspaceService;
    private final CodeGenerationService codeGenerationService;
    private final McpToolService mcpToolService;
    private final ProjectServiceClient projectServiceClient;
    private final TaskTimeoutService taskTimeoutService;
    private final ExecutionHistoryService executionHistoryService;
    private final ProjectContextService projectContextService;
    private final AgentConfig agentConfig;

    @Qualifier("agentTaskExecutor")
    private final Executor agentTaskExecutor;

    /**
     * 正在执行的任务（防止重复执行）
     */
    private final Set<Long> executingTasks = ConcurrentHashMap.newKeySet();

    @Override
    public void executeTask(Long taskId) {
        executeTask(taskId, null);
    }

    @Override
    public void executeTask(Long taskId, String token) {
        // 检查是否正在执行
        if (isTaskExecuting(taskId)) {
            log.warn("任务正在执行中，跳过重复执行, taskId: {}", taskId);
            return;
        }
        executeTaskAsync(taskId, token);
    }

    /**
     * 检查任务是否正在执行
     */
    public boolean isTaskExecuting(Long taskId) {
        return executingTasks.contains(taskId) || taskTimeoutService.isTaskRunning(taskId);
    }

    public CompletableFuture<TaskExecutionResult> executeTaskAsync(Long taskId, String token) {
        // 检查依赖任务是否完成
        String dependencyError = checkDependencies(taskId);
        if (dependencyError != null) {
            log.warn("前置任务未完成，跳过执行, taskId: {}, error: {}", taskId, dependencyError);
            // 记录执行失败
            Long executionId = executionHistoryService.createExecution(taskId);
            executionHistoryService.startExecution(executionId);
            executionHistoryService.failExecution(executionId, dependencyError);
            // 更新任务状态为阻塞
            try {
                projectServiceClient.blockTask(taskId, dependencyError);
            } catch (Exception ex) {
                log.error("更新任务状态失败", ex);
            }
            return CompletableFuture.completedFuture(TaskExecutionResult.failed(dependencyError));
        }

        // 标记任务为执行中
        executingTasks.add(taskId);
        Long executionId = executionHistoryService.createExecution(taskId);

        // 获取当前请求的 Token（在异步执行前保存）
        final String authToken = token != null ? token : getCurrentToken();
        log.debug("保存 Token 用于异步执行: {}", authToken != null ? "已获取" : "未获取");

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 在异步线程中设置 Token
                if (authToken != null) {
                    FeignAuthConfig.setToken(authToken);
                }

                // 获取配置的超时时间
                Duration timeout = getTaskTimeout(taskId);
                return taskTimeoutService.executeWithTimeout(taskId, () -> doExecuteTask(taskId, executionId), timeout);
            } finally {
                // 清除 Token 并移除执行中标记
                FeignAuthConfig.clearToken();
                executingTasks.remove(taskId);
            }
        }, agentTaskExecutor).exceptionally(e -> {
            log.error("任务执行异常, taskId: {}", taskId, e);
            executingTasks.remove(taskId);
            executionHistoryService.failExecution(executionId, "执行异常: " + e.getMessage());
            return TaskExecutionResult.failed("执行异常: " + e.getMessage());
        });
    }

    /**
     * 获取当前请求的 Token
     */
    private String getCurrentToken() {
        try {
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            log.debug("获取 Token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 同步执行任务（用于项目级任务顺序执行）
     */
    private TaskExecutionResult executeTaskSync(Long taskId) {
        // 检查依赖任务是否完成
        String dependencyError = checkDependencies(taskId);
        if (dependencyError != null) {
            log.warn("前置任务未完成，跳过执行, taskId: {}, error: {}", taskId, dependencyError);
            Long executionId = executionHistoryService.createExecution(taskId);
            executionHistoryService.startExecution(executionId);
            executionHistoryService.failExecution(executionId, dependencyError);
            try {
                projectServiceClient.blockTask(taskId, dependencyError);
            } catch (Exception ex) {
                log.error("更新任务状态失败", ex);
            }
            return TaskExecutionResult.failed(dependencyError);
        }

        // 标记任务为执行中
        executingTasks.add(taskId);
        Long executionId = executionHistoryService.createExecution(taskId);

        try {
            Duration timeout = getTaskTimeout(taskId);
            return taskTimeoutService.executeWithTimeout(taskId, () -> {
                return doExecuteTask(taskId, executionId);
            }, timeout);
        } finally {
            executingTasks.remove(taskId);
        }
    }

    /**
     * 检查依赖任务是否全部完成
     * @param taskId 当前任务ID
     * @return 错误信息（null 表示依赖已满足）
     */
    private String checkDependencies(Long taskId) {
        TaskContext taskContext = getTaskContext(taskId);
        List<Long> dependencies = taskContext.getDependencies();

        if (dependencies == null || dependencies.isEmpty()) {
            return null; // 无依赖
        }

        List<Long> unfinishedTasks = new ArrayList<>();

        for (Long depTaskId : dependencies) {
            try {
                Result<Object> result = projectServiceClient.getTask(depTaskId);
                if (result.getCode() == 200 && result.getData() != null) {
                    Map<String, Object> data = (Map<String, Object>) result.getData();
                    Integer status = data.get("status") != null ?
                            Integer.parseInt(data.get("status").toString()) : null;

                    // 状态 4 = DONE
                    if (status == null || status != 4) {
                        unfinishedTasks.add(depTaskId);
                    }
                } else {
                    unfinishedTasks.add(depTaskId);
                }
            } catch (Exception e) {
                log.warn("检查依赖任务失败, depTaskId: {}", depTaskId, e);
                unfinishedTasks.add(depTaskId);
            }
        }

        if (!unfinishedTasks.isEmpty()) {
            return String.format("前置任务未完成: %s，请等待前置任务执行完成后再试", unfinishedTasks);
        }

        return null;
    }

    /**
     * 获取任务超时时间（根据任务复杂度动态调整）
     */
    private Duration getTaskTimeout(Long taskId) {
        // 默认 30 分钟
        long defaultTimeout = agentConfig.getExecution().getTimeout();
        if (defaultTimeout <= 0) {
            defaultTimeout = 30 * 60 * 1000; // 30 分钟
        }
        return Duration.ofMillis(defaultTimeout);
    }

    /**
     * 获取最大迭代次数
     */
    private int getMaxIterations() {
        int maxRetries = agentConfig.getExecution().getMaxRetries();
        return maxRetries > 0 ? maxRetries : 5;
    }

    /**
     * 实际执行任务逻辑
     */
    private TaskExecutionResult doExecuteTask(Long taskId, Long executionId) {
        log.info("开始执行任务, taskId: {}, executionId: {}", taskId, executionId);
        long startTime = System.currentTimeMillis();

        String workspacePath = null;
        String branchName = null;
        int stepOrder = 0;

        try {
            // 1. 开始执行
            executionHistoryService.startExecution(executionId);
            projectServiceClient.startTask(taskId);
            log.info("任务状态更新为 DOING");

            // 2. 获取任务详情（包含项目ID、依赖关系）
            TaskContext taskContext = getTaskContext(taskId);
            log.info("获取任务详情, taskId: {}, projectId: {}, dependencies: {}",
                    taskId, taskContext.getProjectId(), taskContext.getDependencies());

            // 3. 创建工作区（克隆仓库）
            Long step1 = executionHistoryService.addStep(executionId, "创建工作区", ++stepOrder);
            executionHistoryService.updateStepStatus(step1, 1, "执行中");
            workspacePath = workspaceService.createWorkspace(taskId);
            executionHistoryService.completeStep(step1, "工作区路径: " + workspacePath);
            log.info("工作区创建完成: {}", workspacePath);

            // 4. 创建功能分支
            Long step2 = executionHistoryService.addStep(executionId, "创建功能分支", ++stepOrder);
            executionHistoryService.updateStepStatus(step2, 1, "执行中");
            branchName = "agent/task-" + taskId + "-" + System.currentTimeMillis();
            workspaceService.createBranch(workspacePath, branchName);
            executionHistoryService.completeStep(step2, "分支名: " + branchName);
            log.info("功能分支创建完成: {}", branchName);

            // 5. 读取前序任务产出（实现任务连贯性）
            Long step3 = executionHistoryService.addStep(executionId, "读取上下文", ++stepOrder);
            executionHistoryService.updateStepStatus(step3, 1, "执行中");
            String previousTaskContext = getPreviousTaskContext(taskContext);
            String projectContext = "";
            if (taskContext.getProjectId() != null) {
                projectContext = projectContextService.getFullContext(taskContext.getProjectId());
            }
            String fullContext = projectContext + "\n\n" + previousTaskContext;
            executionHistoryService.completeStep(step3, "上下文长度: " + fullContext.length());
            log.info("上下文读取完成, length: {}", fullContext.length());

            // 6. 多轮迭代生成代码
            int maxIterations = getMaxIterations();
            Long step4 = executionHistoryService.addStep(executionId, "生成代码", ++stepOrder);
            executionHistoryService.updateStepStatus(step4, 1, "执行中");

            boolean codeGenerated = false;
            String lastError = null;
            Map<String, String> codeFiles = new HashMap<>();

            for (int iteration = 1; iteration <= maxIterations; iteration++) {
                log.info("代码生成迭代 {}/{}, taskId: {}", iteration, maxIterations, taskId);

                try {
                    // 生成代码
                    if (iteration == 1) {
                        // 第一次：完整生成
                        if (taskContext.getProjectId() != null) {
                            codeFiles = codeGenerationService.generateCodeWithProjectContext(
                                    taskContext.getProjectId(), taskContext.getFullDescription());
                        } else {
                            String context = mcpToolService.knowledgeSearch(
                                    "Java Spring Boot 项目结构、Controller、Service、Mapper 代码示例");
                            codeFiles = codeGenerationService.generateCode(context, taskContext.getFullDescription());
                        }
                    } else {
                        // 后续迭代：修复代码
                        codeFiles = codeGenerationService.fixCodeErrors(fullContext, codeFiles, lastError);
                    }

                    // 写入文件
                    for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
                        workspaceService.writeFile(workspacePath, entry.getKey(), entry.getValue());
                    }
                    log.info("文件写入完成, fileCount: {}", codeFiles.size());

                    // 7. 自检机制：编译检查
                    boolean compileResult = workspaceService.compile(workspacePath);
                    if (!compileResult) {
                        lastError = workspaceService.getCompileError(workspacePath);
                        log.warn("编译失败，尝试修复, iteration: {}, error: {}", iteration, lastError);
                        continue;
                    }

                    // 8. 自检机制：静态分析
                    Long stepCheck = executionHistoryService.addStep(executionId, "静态分析", ++stepOrder);
                    executionHistoryService.updateStepStatus(stepCheck, 1, "执行中");
                    String staticAnalysisResult = performStaticAnalysis(workspacePath, codeFiles);
                    executionHistoryService.completeStep(stepCheck, staticAnalysisResult);

                    // 9. 自检机制：单元测试
                    Long stepTest = executionHistoryService.addStep(executionId, "单元测试", ++stepOrder);
                    executionHistoryService.updateStepStatus(stepTest, 1, "执行中");
                    boolean testResult = workspaceService.test(workspacePath);
                    if (!testResult) {
                        log.warn("单元测试失败，但继续执行");
                        executionHistoryService.completeStep(stepTest, "测试失败（非阻塞）");
                    } else {
                        executionHistoryService.completeStep(stepTest, "测试通过");
                    }

                    codeGenerated = true;
                    executionHistoryService.completeStep(step4, "第 " + iteration + " 次迭代成功，共 " + codeFiles.size() + " 个文件");
                    break;

                } catch (Exception e) {
                    lastError = e.getMessage();
                    log.error("代码生成迭代失败, iteration: {}", iteration, e);
                }
            }

            if (!codeGenerated) {
                executionHistoryService.failStep(step4, "代码生成失败: " + lastError);
                throw new RuntimeException("代码生成失败，已尝试 " + maxIterations + " 次: " + lastError);
            }

            // 10. Git Commit 到功能分支
            Long step5 = executionHistoryService.addStep(executionId, "Git 提交", ++stepOrder);
            executionHistoryService.updateStepStatus(step5, 1, "执行中");
            String commitMessage = buildCommitMessage(taskId, taskContext);
            workspaceService.commit(workspacePath, commitMessage);
            executionHistoryService.completeStep(step5, "提交到分支: " + branchName);
            log.info("Git 提交完成");

            // 11. 推送分支
            Long step6 = executionHistoryService.addStep(executionId, "推送分支", ++stepOrder);
            executionHistoryService.updateStepStatus(step6, 1, "执行中");
            workspaceService.push(workspacePath, branchName);
            executionHistoryService.completeStep(step6, "分支已推送");
            log.info("分支推送完成");

            // 12. 创建 Pull Request
            Long step7 = executionHistoryService.addStep(executionId, "创建 Pull Request", ++stepOrder);
            executionHistoryService.updateStepStatus(step7, 1, "执行中");
            String prUrl = createPullRequest(workspacePath, branchName, taskId, taskContext, commitMessage);
            executionHistoryService.completeStep(step7, "PR: " + prUrl);
            log.info("Pull Request 创建完成: {}", prUrl);

            // 13. 更新任务状态为完成
            projectServiceClient.completeTask(taskId);
            log.info("任务状态更新为 DONE");

            // 14. 记录执行完成
            long duration = System.currentTimeMillis() - startTime;
            executionHistoryService.completeExecution(executionId,
                    String.format("执行成功，耗时 %dms，分支: %s，PR: %s", duration, branchName, prUrl));
            log.info("任务执行完成, taskId: {}, duration: {}ms", taskId, duration);

            return TaskExecutionResult.success("执行成功，PR: " + prUrl);

        } catch (Exception e) {
            log.error("任务执行失败, taskId: {}", taskId, e);

            try {
                projectServiceClient.blockTask(taskId, "Agent 执行失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("更新任务状态失败", ex);
            }

            executionHistoryService.failExecution(executionId, e.getMessage());
            return TaskExecutionResult.failed(e.getMessage());
        }
    }

    /**
     * 获取任务上下文（包含依赖关系）
     */
    private TaskContext getTaskContext(Long taskId) {
        TaskContext context = new TaskContext();
        context.setTaskId(taskId);

        try {
            Result<Object> result = projectServiceClient.getTask(taskId);
            if (result.getCode() == 200 && result.getData() != null) {
                Map<String, Object> data = (Map<String, Object>) result.getData();
                context.setProjectId(data.get("projectId") != null ?
                        Long.valueOf(data.get("projectId").toString()) : null);
                context.setTitle((String) data.get("title"));
                context.setDescription((String) data.get("description"));
                context.setSortOrder(data.get("sortOrder") != null ?
                        Integer.parseInt(data.get("sortOrder").toString()) : 0);

                // 获取依赖的任务ID列表
                if (data.get("dependencies") != null) {
                    List<?> deps = (List<?>) data.get("dependencies");
                    context.setDependencies(deps.stream()
                            .map(d -> Long.valueOf(d.toString()))
                            .collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            log.warn("获取任务上下文失败", e);
        }

        return context;
    }

    /**
     * 获取前序任务的产出（实现任务连贯性）
     */
    private String getPreviousTaskContext(TaskContext taskContext) {
        if (taskContext.getDependencies() == null || taskContext.getDependencies().isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("## 前序任务产出\n");

        for (Long depTaskId : taskContext.getDependencies()) {
            try {
                // 获取前序任务的执行记录
                List<com.platform.agent.domain.entity.AgentExecution> executions =
                        executionHistoryService.getExecutionsByTaskId(depTaskId);

                if (executions != null && !executions.isEmpty()) {
                    // 获取最新的成功执行
                    Optional<com.platform.agent.domain.entity.AgentExecution> successExec = executions.stream()
                            .filter(e -> e.getStatus() == 2) // 成功状态
                            .findFirst();

                    if (successExec.isPresent()) {
                        context.append("### 任务 ").append(depTaskId).append("\n");
                        context.append("- 执行结果: ").append(successExec.get().getResultSummary()).append("\n");

                        // 获取步骤详情
                        if (successExec.get().getSteps() != null) {
                            context.append("- 执行步骤:\n");
                            for (var step : successExec.get().getSteps()) {
                                context.append("  - ").append(step.getStepName())
                                        .append(": ").append(step.getDetail()).append("\n");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取前序任务产出失败, depTaskId: {}", depTaskId, e);
            }
        }

        return context.toString();
    }

    /**
     * 执行静态分析
     */
    private String performStaticAnalysis(String workspacePath, Map<String, String> codeFiles) {
        StringBuilder result = new StringBuilder();

        // 检查代码规范
        for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            // 检查基本规范
            List<String> issues = new ArrayList<>();

            // 检查是否有 TODO
            if (content.contains("TODO")) {
                issues.add("包含 TODO 注释");
            }

            // 检查是否有 System.out
            if (content.contains("System.out.print")) {
                issues.add("使用了 System.out，建议使用日志");
            }

            // 检查异常处理
            if (content.contains("catch (Exception e)") && !content.contains("log.")) {
                issues.add("捕获异常但未记录日志");
            }

            if (!issues.isEmpty()) {
                result.append(filePath).append(": ").append(String.join(", ", issues)).append("; ");
            }
        }

        if (result.isEmpty()) {
            result.append("静态分析通过");
        }

        return result.toString();
    }

    /**
     * 构建提交信息
     */
    private String buildCommitMessage(Long taskId, TaskContext taskContext) {
        return String.format(
                "feat(agent): 实现任务 #%d - %s\n\n%s\n\nTask-Id: %d\nProject-Id: %s",
                taskId,
                taskContext.getTitle() != null ? taskContext.getTitle() : "未知任务",
                taskContext.getDescription() != null ? taskContext.getDescription() : "",
                taskId,
                taskContext.getProjectId()
        );
    }

    /**
     * 创建 Pull Request
     */
    private String createPullRequest(String workspacePath, String branchName,
                                     Long taskId, TaskContext taskContext, String commitMessage) {
        try {
            String title = String.format("feat: 实现任务 #%d - %s", taskId,
                    taskContext.getTitle() != null ? taskContext.getTitle() : "未知任务");
            String description = buildPRDescription(taskId, taskContext, commitMessage);
            return workspaceService.createPullRequest(workspacePath, branchName, "main", title, description);
        } catch (Exception e) {
            log.warn("创建 PR 失败: {}", e.getMessage());
            return "PR 创建跳过: " + e.getMessage();
        }
    }

    /**
     * 构建 PR 描述
     */
    private String buildPRDescription(Long taskId, TaskContext taskContext, String commitMessage) {
        return String.format(
                "## 🤖 Agent 自动执行\n\n" +
                "### 任务信息\n" +
                "- 任务 ID: %d\n" +
                "- 任务标题: %s\n" +
                "- 任务描述: %s\n" +
                "- 项目 ID: %s\n\n" +
                "### 执行信息\n" +
                "- 排序顺序: %d\n" +
                "- 依赖任务: %s\n\n" +
                "### 提交信息\n" +
                "```\n%s\n```\n\n" +
                "### 审查要点\n" +
                "- [ ] 代码逻辑是否正确\n" +
                "- [ ] 是否符合项目规范\n" +
                "- [ ] 测试是否通过\n" +
                "- [ ] 是否有安全隐患\n\n" +
                "---\n" +
                "*此 PR 由 Agent 自动生成*",
                taskId,
                taskContext.getTitle(),
                taskContext.getDescription(),
                taskContext.getProjectId(),
                taskContext.getSortOrder(),
                taskContext.getDependencies(),
                commitMessage
        );
    }

    @Override
    public int executeProjectTasks(Long projectId) {
        log.info("开始执行项目任务, projectId: {}", projectId);
        int executedCount = 0;

        try {
            Result<Map<String, Object>> result = projectServiceClient.getTaskList(projectId, 0, 100);
            if (result.getCode() != 200 || result.getData() == null) {
                log.error("获取项目任务失败");
                return 0;
            }

            Map<String, Object> data = result.getData();
            List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
            if (records == null || records.isEmpty()) {
                log.info("项目没有待执行任务, projectId: {}", projectId);
                return 0;
            }

            log.info("获取到 {} 个待执行任务, projectId: {}", records.size(), projectId);

            // 按 sortOrder 排序
            records.sort((a, b) -> {
                int orderA = a.get("sortOrder") != null ? Integer.parseInt(a.get("sortOrder").toString()) : Integer.MAX_VALUE;
                int orderB = b.get("sortOrder") != null ? Integer.parseInt(b.get("sortOrder").toString()) : Integer.MAX_VALUE;
                return Integer.compare(orderA, orderB);
            });

            // 记录失败的任务ID（用于后续依赖检查）
            Set<Long> failedTaskIds = new HashSet<>();

            // 顺序执行任务（确保依赖关系）
            for (Map<String, Object> task : records) {
                Long taskId = Long.valueOf(task.get("id").toString());

                // 检查是否正在执行
                if (isTaskExecuting(taskId)) {
                    log.warn("任务正在执行中，跳过, taskId: {}", taskId);
                    continue;
                }

                // 检查依赖任务是否完成
                String dependencyError = checkDependencies(taskId);
                if (dependencyError != null) {
                    log.warn("前置任务未完成，跳过执行, taskId: {}, error: {}", taskId, dependencyError);
                    failedTaskIds.add(taskId);
                    try {
                        projectServiceClient.blockTask(taskId, dependencyError);
                    } catch (Exception ex) {
                        log.error("更新任务状态失败", ex);
                    }
                    continue;
                }

                // 检查依赖的任务是否在失败列表中
                TaskContext taskContext = getTaskContext(taskId);
                boolean hasFailedDependency = taskContext.getDependencies().stream()
                        .anyMatch(failedTaskIds::contains);

                if (hasFailedDependency) {
                    log.warn("前置任务执行失败，跳过执行, taskId: {}", taskId);
                    failedTaskIds.add(taskId);
                    try {
                        projectServiceClient.blockTask(taskId, "前置任务执行失败");
                    } catch (Exception ex) {
                        log.error("更新任务状态失败", ex);
                    }
                    continue;
                }

                try {
                    log.info("执行任务, taskId: {}", taskId);
                    // 使用同步执行，确保任务按顺序完成
                    TaskExecutionResult taskResult = executeTaskSync(taskId);
                    if (taskResult.isSuccess()) {
                        executedCount++;
                    } else {
                        failedTaskIds.add(taskId);
                        log.error("任务执行失败, taskId: {}, error: {}", taskId, taskResult.getMessage());
                    }
                } catch (Exception e) {
                    failedTaskIds.add(taskId);
                    log.error("执行任务异常, taskId: {}", taskId, e);
                }
            }

            log.info("项目任务执行完成, projectId: {}, executedCount: {}", projectId, executedCount);
            return executedCount;
        } catch (Exception e) {
            log.error("执行项目任务失败, projectId: {}", projectId, e);
            return executedCount;
        }
    }

    /**
     * 取消任务执行
     */
    public boolean cancelTask(Long taskId) {
        log.info("取消任务执行, taskId: {}", taskId);
        executingTasks.remove(taskId);
        return taskTimeoutService.cancelTask(taskId);
    }

    /**
     * 任务上下文内部类
     */
    private static class TaskContext {
        private Long taskId;
        private Long projectId;
        private String title;
        private String description;
        private int sortOrder;
        private List<Long> dependencies = new ArrayList<>();

        public String getFullDescription() {
            StringBuilder desc = new StringBuilder();
            desc.append("任务标题: ").append(title != null ? title : "未知").append("\n");
            if (description != null) {
                desc.append("任务描述: ").append(description).append("\n");
            }
            if (!dependencies.isEmpty()) {
                desc.append("依赖任务: ").append(dependencies).append("\n");
            }
            return desc.toString();
        }

        // Getters and Setters
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getSortOrder() { return sortOrder; }
        public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
        public List<Long> getDependencies() { return dependencies; }
        public void setDependencies(List<Long> dependencies) { this.dependencies = dependencies; }
    }
}
