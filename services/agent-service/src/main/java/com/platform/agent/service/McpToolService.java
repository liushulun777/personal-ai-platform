package com.platform.agent.service;

import java.util.Map;

/**
 * MCP 工具调用服务接口
 */
public interface McpToolService {

    // ==================== 文件系统工具 ====================

    /**
     * 读取文件
     *
     * @param path 文件路径
     * @return 文件内容
     */
    String readFile(String path);

    /**
     * 写入文件
     *
     * @param path    文件路径
     * @param content 文件内容
     */
    void writeFile(String path, String content);

    /**
     * 创建目录
     *
     * @param path 目录路径
     */
    void mkdir(String path);

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录路径
     */
    void delete(String path);

    // ==================== Git 工具 ====================

    /**
     * Git Clone
     *
     * @param repoUrl 仓库地址
     * @param targetPath 目标路径
     */
    void gitClone(String repoUrl, String targetPath);

    /**
     * Git Commit
     *
     * @param workDir 工作目录
     * @param message 提交信息
     */
    void gitCommit(String workDir, String message);

    /**
     * Git Checkout
     *
     * @param workDir 工作目录
     * @param branch  分支名
     */
    void gitCheckout(String workDir, String branch);

    /**
     * Git Diff
     *
     * @param workDir 工作目录
     * @return diff 内容
     */
    String gitDiff(String workDir);

    // ==================== 终端工具 ====================

    /**
     * 执行命令
     *
     * @param workDir 工作目录
     * @param command 命令
     * @return 命令输出
     */
    String exec(String workDir, String command);

    // ==================== 数据库工具 ====================

    /**
     * 执行查询
     *
     * @param sql SQL 语句
     * @return 查询结果
     */
    Map<String, Object> query(String sql);

    /**
     * 执行更新
     *
     * @param sql SQL 语句
     * @return 影响行数
     */
    int execute(String sql);

    // ==================== HTTP 工具 ====================

    /**
     * HTTP GET
     *
     * @param url URL
     * @return 响应内容
     */
    String httpGet(String url);

    /**
     * HTTP POST
     *
     * @param url     URL
     * @param body    请求体
     * @return 响应内容
     */
    String httpPost(String url, String body);

    // ==================== Docker 工具 ====================

    /**
     * Docker Build
     *
     * @param workDir   工作目录
     * @param dockerfile Dockerfile 路径
     * @param tag        镜像标签
     */
    void dockerBuild(String workDir, String dockerfile, String tag);

    /**
     * Docker Run
     *
     * @param image 镜像名称
     * @param args  运行参数
     * @return 容器 ID
     */
    String dockerRun(String image, String args);

    // ==================== 知识库工具 ====================

    /**
     * 知识库搜索
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    String knowledgeSearch(String query);
}
