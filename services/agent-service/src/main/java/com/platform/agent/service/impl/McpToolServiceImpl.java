package com.platform.agent.service.impl;

import com.platform.agent.client.KnowledgeServiceClient;
import com.platform.agent.service.McpToolService;
import com.platform.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP 工具调用服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolServiceImpl implements McpToolService {

    private final KnowledgeServiceClient knowledgeServiceClient;

    // ==================== 文件系统工具 ====================

    @Override
    public String readFile(String path) {
        try {
            log.info("读取文件: {}", path);
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            log.error("读取文件失败: {}", path, e);
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeFile(String path, String content) {
        try {
            log.info("写入文件: {}", path);
            Path filePath = Paths.get(path);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content);
        } catch (IOException e) {
            log.error("写入文件失败: {}", path, e);
            throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void mkdir(String path) {
        try {
            log.info("创建目录: {}", path);
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            log.error("创建目录失败: {}", path, e);
            throw new RuntimeException("创建目录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            log.info("删除: {}", path);
            Path filePath = Paths.get(path);
            if (Files.isDirectory(filePath)) {
                deleteDirectory(filePath.toFile());
            } else {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.error("删除失败: {}", path, e);
            throw new RuntimeException("删除失败: " + e.getMessage(), e);
        }
    }

    // ==================== Git 工具 ====================

    @Override
    public void gitClone(String repoUrl, String targetPath) {
        log.info("Git Clone: {} -> {}", repoUrl, targetPath);
        exec(".", "git clone " + repoUrl + " " + targetPath);
    }

    @Override
    public void gitCommit(String workDir, String message) {
        log.info("Git Commit: {}", message);
        exec(workDir, "git add .");
        exec(workDir, "git commit -m \"" + message + "\"");
    }

    @Override
    public void gitCheckout(String workDir, String branch) {
        log.info("Git Checkout: {}", branch);
        exec(workDir, "git checkout " + branch);
    }

    @Override
    public String gitDiff(String workDir) {
        log.info("Git Diff");
        return exec(workDir, "git diff");
    }

    // ==================== 终端工具 ====================

    /**
     * 检测操作系统是否为 Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public String exec(String workDir, String command) {
        try {
            log.info("执行命令: {} (workDir: {})", command, workDir);

            ProcessBuilder pb;
            if (isWindows()) {
                // Windows 环境使用 cmd
                pb = new ProcessBuilder("cmd", "/c", command);
            } else {
                // Linux/Mac 环境使用 bash
                pb = new ProcessBuilder("bash", "-c", command);
            }

            // 设置工作目录
            if (workDir != null && !workDir.equals(".")) {
                pb.directory(new File(workDir));
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            log.info("命令执行完成, exitCode: {}", exitCode);
            if (exitCode != 0) {
                log.warn("命令输出: {}", output);
            }
            return output;
        } catch (IOException | InterruptedException e) {
            log.error("执行命令失败: {}", command, e);
            throw new RuntimeException("执行命令失败: " + e.getMessage(), e);
        }
    }

    // ==================== 数据库工具 ====================

    @Override
    public Map<String, Object> query(String sql) {
        log.info("执行查询: {}", sql);
        // TODO: 实现数据库查询
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "查询执行成功");
        return result;
    }

    @Override
    public int execute(String sql) {
        log.info("执行更新: {}", sql);
        // TODO: 实现数据库更新
        return 1;
    }

    // ==================== HTTP 工具 ====================

    @Override
    public String httpGet(String url) {
        try {
            log.info("HTTP GET: {}", url);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            log.error("HTTP GET 失败: {}", url, e);
            throw new RuntimeException("HTTP GET 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String httpPost(String url, String body) {
        try {
            log.info("HTTP POST: {}", url);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST 失败: {}", url, e);
            throw new RuntimeException("HTTP POST 失败: " + e.getMessage(), e);
        }
    }

    // ==================== Docker 工具 ====================

    @Override
    public void dockerBuild(String workDir, String dockerfile, String tag) {
        log.info("Docker Build: {}", tag);
        exec(workDir, "docker build -f " + dockerfile + " -t " + tag + " .");
    }

    @Override
    public String dockerRun(String image, String args) {
        log.info("Docker Run: {}", image);
        return exec(".", "docker run " + args + " " + image);
    }

    // ==================== 知识库工具 ====================

    @Override
    public String knowledgeSearch(String query) {
        try {
            log.info("知识库搜索: {}", query);
            Map<String, Object> queryDTO = new HashMap<>();
            queryDTO.put("question", query);
            queryDTO.put("topK", 5);

            Result<Map<String, Object>> result = knowledgeServiceClient.query(queryDTO);
            if (result.getCode() == 200 && result.getData() != null) {
                Map<String, Object> data = result.getData();
                Object answer = data.get("answer");
                return answer != null ? answer.toString() : "";
            }
            return "";
        } catch (Exception e) {
            log.error("知识库搜索失败", e);
            return "";
        }
    }

    // ==================== 辅助方法 ====================

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}
