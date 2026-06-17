package com.platform.agent.service.impl;

import com.platform.agent.config.AgentConfig;
import com.platform.agent.service.McpToolService;
import com.platform.agent.service.ProjectContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 项目上下文服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectContextServiceImpl implements ProjectContextService {

    private final AgentConfig agentConfig;
    private final McpToolService mcpToolService;

    @Override
    public String getProjectStructure(Long projectId) {
        String workspacePath = getWorkspacePath(projectId);
        return readDirectoryStructure(workspacePath, 3);
    }

    @Override
    public String getProjectTechStack(Long projectId) {
        String workspacePath = getWorkspacePath(projectId);
        StringBuilder techStack = new StringBuilder();

        // 检查 pom.xml（Maven 项目）
        String pomPath = workspacePath + "/pom.xml";
        if (Files.exists(Paths.get(pomPath))) {
            try {
                String pomContent = mcpToolService.readFile(pomPath);
                techStack.append("## 构建工具: Maven\n");
                techStack.append("### 关键依赖:\n");
                techStack.append(extractMavenDependencies(pomContent));
                techStack.append("\n");
            } catch (Exception e) {
                log.warn("读取 pom.xml 失败: {}", e.getMessage());
            }
        }

        // 检查 package.json（前端项目）
        String packageJsonPath = workspacePath + "/package.json";
        if (Files.exists(Paths.get(packageJsonPath))) {
            try {
                String packageContent = mcpToolService.readFile(packageJsonPath);
                techStack.append("## 前端项目\n");
                techStack.append("### 关键依赖:\n");
                techStack.append(extractNpmDependencies(packageContent));
                techStack.append("\n");
            } catch (Exception e) {
                log.warn("读取 package.json 失败: {}", e.getMessage());
            }
        }

        // 检查 application.yml（Spring Boot 配置）
        String appYmlPath = findApplicationYml(workspacePath);
        if (appYmlPath != null) {
            try {
                String ymlContent = mcpToolService.readFile(appYmlPath);
                techStack.append("## Spring Boot 配置\n");
                techStack.append(extractSpringConfig(ymlContent));
                techStack.append("\n");
            } catch (Exception e) {
                log.warn("读取 application.yml 失败: {}", e.getMessage());
            }
        }

        return techStack.toString();
    }

    @Override
    public String getFullContext(Long projectId) {
        StringBuilder context = new StringBuilder();

        // 1. 项目结构
        context.append("# 项目结构\n");
        context.append(getProjectStructure(projectId));
        context.append("\n\n");

        // 2. 技术栈
        context.append("# 技术栈\n");
        context.append(getProjectTechStack(projectId));
        context.append("\n\n");

        // 3. 关键代码文件
        context.append("# 关键代码示例\n");
        context.append(getKeyCodeSamples(projectId));

        return context.toString();
    }

    @Override
    public String readDirectoryStructure(String workspacePath, int maxDepth) {
        try {
            Path root = Paths.get(workspacePath);
            if (!Files.exists(root)) {
                return "工作区不存在: " + workspacePath;
            }

            StringBuilder structure = new StringBuilder();
            buildDirectoryTree(root, "", maxDepth, structure);
            return structure.toString();
        } catch (Exception e) {
            log.error("读取目录结构失败: {}", workspacePath, e);
            return "读取目录结构失败: " + e.getMessage();
        }
    }

    /**
     * 递归构建目录树
     */
    private void buildDirectoryTree(Path path, String prefix, int maxDepth, StringBuilder result) throws IOException {
        if (maxDepth <= 0) {
            return;
        }

        try (Stream<Path> entries = Files.list(path)) {
            var sortedEntries = entries
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .filter(p -> !p.getFileName().toString().equals("node_modules"))
                    .filter(p -> !p.getFileName().toString().equals("target"))
                    .filter(p -> !p.getFileName().toString().equals("build"))
                    .sorted((a, b) -> {
                        boolean aDir = Files.isDirectory(a);
                        boolean bDir = Files.isDirectory(b);
                        if (aDir && !bDir) return -1;
                        if (!aDir && bDir) return 1;
                        return a.getFileName().toString().compareTo(b.getFileName().toString());
                    })
                    .collect(Collectors.toList());

            int size = sortedEntries.size();
            for (int i = 0; i < size; i++) {
                Path entry = sortedEntries.get(i);
                boolean isLast = (i == size - 1);
                String connector = isLast ? "└── " : "├── ";
                String childPrefix = isLast ? "    " : "│   ";

                String name = entry.getFileName().toString();
                if (Files.isDirectory(entry)) {
                    result.append(prefix).append(connector).append(name).append("/\n");
                    buildDirectoryTree(entry, prefix + childPrefix, maxDepth - 1, result);
                } else {
                    result.append(prefix).append(connector).append(name).append("\n");
                }
            }
        }
    }

    /**
     * 提取 Maven 依赖
     */
    private String extractMavenDependencies(String pomContent) {
        StringBuilder deps = new StringBuilder();
        String[] lines = pomContent.split("\n");
        boolean inDeps = false;
        String groupId = "";
        String artifactId = "";

        for (String line : lines) {
            line = line.trim();
            if (line.contains("<dependencies>")) {
                inDeps = true;
                continue;
            }
            if (line.contains("</dependencies>")) {
                inDeps = false;
                continue;
            }
            if (inDeps) {
                if (line.contains("<groupId>")) {
                    groupId = extractTagContent(line, "groupId");
                }
                if (line.contains("<artifactId>")) {
                    artifactId = extractTagContent(line, "artifactId");
                    if (!groupId.startsWith("${") && !artifactId.startsWith("${")) {
                        deps.append("- ").append(groupId).append(":").append(artifactId).append("\n");
                    }
                }
            }
        }

        return deps.toString();
    }

    /**
     * 提取 npm 依赖
     */
    private String extractNpmDependencies(String packageContent) {
        // 简单提取 dependencies 和 devDependencies
        StringBuilder deps = new StringBuilder();
        // 这里可以使用 JSON 解析库来更精确地提取
        // 简单实现：返回部分内容
        if (packageContent.contains("\"dependencies\"")) {
            deps.append("- 生产依赖已配置\n");
        }
        if (packageContent.contains("\"devDependencies\"")) {
            deps.append("- 开发依赖已配置\n");
        }
        return deps.toString();
    }

    /**
     * 提取 Spring 配置
     */
    private String extractSpringConfig(String ymlContent) {
        StringBuilder config = new StringBuilder();
        String[] lines = ymlContent.split("\n");

        for (String line : lines) {
            line = line.trim();
            // 提取关键配置
            if (line.contains("spring.datasource") || line.contains("spring.data.redis") ||
                line.contains("spring.kafka") || line.contains("mybatis-plus")) {
                config.append("- ").append(line).append("\n");
            }
        }

        return config.toString();
    }

    /**
     * 获取关键代码示例
     */
    private String getKeyCodeSamples(Long projectId) {
        String workspacePath = getWorkspacePath(projectId);
        StringBuilder samples = new StringBuilder();

        // 读取一个 Controller 示例
        String controllerPath = findFileByPattern(workspacePath, "**/controller/*Controller.java");
        if (controllerPath != null) {
            try {
                String content = mcpToolService.readFile(controllerPath);
                samples.append("### Controller 示例\n```java\n");
                samples.append(content, 0, Math.min(content.length(), 500));
                samples.append("\n```\n\n");
            } catch (Exception e) {
                log.warn("读取 Controller 示例失败: {}", e.getMessage());
            }
        }

        // 读取一个 Service 示例
        String servicePath = findFileByPattern(workspacePath, "**/service/impl/*ServiceImpl.java");
        if (servicePath != null) {
            try {
                String content = mcpToolService.readFile(servicePath);
                samples.append("### Service 示例\n```java\n");
                samples.append(content, 0, Math.min(content.length(), 500));
                samples.append("\n```\n");
            } catch (Exception e) {
                log.warn("读取 Service 示例失败: {}", e.getMessage());
            }
        }

        return samples.toString();
    }

    /**
     * 查找 application.yml 文件
     */
    private String findApplicationYml(String workspacePath) {
        String[] paths = {
            workspacePath + "/src/main/resources/application.yml",
            workspacePath + "/src/main/resources/application.yaml",
            workspacePath + "/src/main/resources/application.properties"
        };

        for (String path : paths) {
            if (Files.exists(Paths.get(path))) {
                return path;
            }
        }
        return null;
    }

    /**
     * 根据模式查找文件
     */
    private String findFileByPattern(String workspacePath, String pattern) {
        try {
            Path root = Paths.get(workspacePath);
            try (Stream<Path> paths = Files.walk(root, 5)) {
                return paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith("Controller.java") || p.toString().endsWith("ServiceImpl.java"))
                        .findFirst()
                        .map(Path::toString)
                        .orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提取 XML 标签内容
     */
    private String extractTagContent(String line, String tag) {
        int start = line.indexOf("<" + tag + ">");
        int end = line.indexOf("</" + tag + ">");
        if (start >= 0 && end > start) {
            return line.substring(start + tag.length() + 2, end).trim();
        }
        return "";
    }

    /**
     * 获取工作区路径
     */
    private String getWorkspacePath(Long projectId) {
        return agentConfig.getWorkspace().getBasePath() + "/project-" + projectId;
    }
}
