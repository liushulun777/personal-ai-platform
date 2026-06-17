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
    public void writeFile(String workspacePath, String filePath, String content) {
        String fullPath = workspacePath + "/" + filePath;
        mcpToolService.writeFile(fullPath, content);
        log.info("写入文件: {}", fullPath);
    }

    @Override
    public boolean compile(String workspacePath) {
        log.info("编译项目: {}", workspacePath);
        String output = mcpToolService.exec(workspacePath, "mvn clean compile -q");
        if (output != null && !output.contains("ERROR")) {
            log.info("编译成功");
            return true;
        } else {
            log.error("编译失败: {}", output);
            return false;
        }
    }

    @Override
    public boolean test(String workspacePath) {
        log.info("执行测试: {}", workspacePath);
        String output = mcpToolService.exec(workspacePath, "mvn test -q");
        if (output != null && !output.contains("ERROR")) {
            log.info("测试通过");
            return true;
        } else {
            log.error("测试失败: {}", output);
            return false;
        }
    }

    @Override
    public void commit(String workspacePath, String message) {
        log.info("Git 提交: {}", workspacePath);
        mcpToolService.gitCommit(workspacePath, message);
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
