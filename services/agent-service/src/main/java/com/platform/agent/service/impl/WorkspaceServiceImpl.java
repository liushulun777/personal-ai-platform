package com.platform.agent.service.impl;

import com.platform.agent.config.AgentConfig;
import com.platform.agent.service.McpToolService;
import com.platform.agent.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 工作区管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final AgentConfig agentConfig;
    private final McpToolService mcpToolService;

    @Override
    public String createWorkspace(Long taskId) {
        String workspacePath = getWorkspacePath(taskId);

        // 创建目录
        mcpToolService.mkdir(workspacePath);
        log.info("创建工作区: {}", workspacePath);

        // 如果配置了 Git 仓库地址，执行 clone
        String gitUrl = agentConfig.getWorkspace().getGitUrl();
        if (gitUrl != null && !gitUrl.isEmpty()) {
            mcpToolService.gitClone(gitUrl, workspacePath);
        }

        return workspacePath;
    }

    @Override
    public void createBranch(String workspacePath, String branchName) {
        log.info("创建功能分支: {} in {}", branchName, workspacePath);
        // 创建并切换到新分支
        mcpToolService.exec(workspacePath, "git checkout -b " + branchName);
    }

    @Override
    public void writeFile(String workspacePath, String filePath, String content) {
        String fullPath = workspacePath + "/" + filePath;
        mcpToolService.writeFile(fullPath, content);
        log.debug("写入文件: {}", fullPath);
    }

    @Override
    public String readFile(String workspacePath, String filePath) {
        String fullPath = workspacePath + "/" + filePath;
        return mcpToolService.readFile(fullPath);
    }

    @Override
    public boolean compile(String workspacePath) {
        log.info("编译项目: {}", workspacePath);
        String output = mcpToolService.exec(workspacePath, "mvn clean compile -q 2>&1");
        if (output != null && !output.contains("ERROR") && !output.contains("FAILURE")) {
            log.info("编译成功");
            return true;
        } else {
            log.warn("编译失败: {}", output);
            return false;
        }
    }

    @Override
    public String getCompileError(String workspacePath) {
        log.info("获取编译错误: {}", workspacePath);
        String output = mcpToolService.exec(workspacePath, "mvn clean compile 2>&1");
        return output != null ? output : "未知错误";
    }

    @Override
    public boolean test(String workspacePath) {
        log.info("执行测试: {}", workspacePath);
        String output = mcpToolService.exec(workspacePath, "mvn test -q 2>&1");
        if (output != null && !output.contains("ERROR") && !output.contains("FAILURE")) {
            log.info("测试通过");
            return true;
        } else {
            log.warn("测试失败: {}", output);
            return false;
        }
    }

    @Override
    public void commit(String workspacePath, String message) {
        log.info("Git 提交: {}", workspacePath);
        // 添加所有变更
        mcpToolService.exec(workspacePath, "git add .");
        // 提交
        String escapedMessage = message.replace("\"", "\\\"");
        mcpToolService.exec(workspacePath, "git commit -m \"" + escapedMessage + "\"");
    }

    @Override
    public void push(String workspacePath, String branchName) {
        log.info("推送分支: {} to origin/{}", branchName, branchName);
        mcpToolService.exec(workspacePath, "git push origin " + branchName);
    }

    @Override
    public String createPullRequest(String workspacePath, String headBranch, String targetBranch, String title, String description) {
        log.info("创建 Pull Request: {} -> {}", headBranch, targetBranch);

        try {
            // 使用文件传递参数避免命令注入
            String tempFile = workspacePath + "/.pr_body.md";
            mcpToolService.writeFile(tempFile, description);

            // 构建命令（使用文件传递 body）
            String command = String.format(
                    "gh pr create --base %s --head %s --title %s --body-file %s",
                    sanitizeBranchName(targetBranch),
                    sanitizeBranchName(headBranch),
                    sanitizeShellArg(title),
                    tempFile
            );

            String output = mcpToolService.exec(workspacePath, command);

            // 清理临时文件
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(tempFile));
            } catch (Exception ignored) {}

            if (output != null && output.contains("https://")) {
                String prUrl = output.trim();
                log.info("Pull Request 创建成功: {}", prUrl);
                return prUrl;
            }

            log.warn("gh CLI 不可用或创建失败，跳过 PR 创建");
            return "PR 创建跳过（gh CLI 不可用）";
        } catch (Exception e) {
            log.warn("创建 PR 失败: {}", e.getMessage());
            return "PR 创建失败: " + e.getMessage();
        }
    }

    /**
     * 清理分支名（只允许字母、数字、-、_、/）
     */
    private String sanitizeBranchName(String branchName) {
        if (branchName == null) return "";
        return branchName.replaceAll("[^a-zA-Z0-9\\-_/.]", "");
    }

    /**
     * 清理 Shell 参数
     */
    private String sanitizeShellArg(String arg) {
        if (arg == null) return "\"\"";
        // 移除危险字符
        String sanitized = arg.replaceAll("[`$\\\\\"", "]");
        return "\"" + sanitized + "\"";
    }

    @Override
    public void cleanup(Long taskId) {
        String workspacePath = getWorkspacePath(taskId);
        try {
            mcpToolService.delete(workspacePath);
            log.info("清理工作区: {}", workspacePath);
        } catch (Exception e) {
            log.error("清理工作区失败: {}", workspacePath, e);
        }
    }

    private String getWorkspacePath(Long taskId) {
        return agentConfig.getWorkspace().getBasePath() + "/task-" + taskId;
    }
}
