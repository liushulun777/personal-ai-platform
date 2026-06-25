package com.platform.agent.service;

/**
 * 工作区管理服务接口
 */
public interface WorkspaceService {

    /**
     * 创建工作区（Git Clone）
     *
     * @param taskId 任务ID
     * @return 工作区路径
     */
    String createWorkspace(Long taskId);

    /**
     * 创建功能分支
     *
     * @param workspacePath 工作区路径
     * @param branchName    分支名
     */
    void createBranch(String workspacePath, String branchName);

    /**
     * 写入文件
     *
     * @param workspacePath 工作区路径
     * @param filePath      文件路径
     * @param content       文件内容
     */
    void writeFile(String workspacePath, String filePath, String content);

    /**
     * 读取文件
     *
     * @param workspacePath 工作区路径
     * @param filePath      文件路径
     * @return 文件内容
     */
    String readFile(String workspacePath, String filePath);

    /**
     * 编译项目
     *
     * @param workspacePath 工作区路径
     * @return 是否成功
     */
    boolean compile(String workspacePath);

    /**
     * 获取编译错误信息
     *
     * @param workspacePath 工作区路径
     * @return 错误信息
     */
    String getCompileError(String workspacePath);

    /**
     * 执行测试
     *
     * @param workspacePath 工作区路径
     * @return 是否成功
     */
    boolean test(String workspacePath);

    /**
     * Git 提交
     *
     * @param workspacePath 工作区路径
     * @param message       提交信息
     */
    void commit(String workspacePath, String message);

    /**
     * 推送分支
     *
     * @param workspacePath 工作区路径
     * @param branchName    分支名
     */
    void push(String workspacePath, String branchName);

    /**
     * 创建 Pull Request
     *
     * @param workspacePath 工作区路径
     * @param headBranch    源分支
     * @param targetBranch  目标分支
     * @param title         PR 标题
     * @param description   PR 描述
     * @return PR URL
     */
    String createPullRequest(String workspacePath, String headBranch, String targetBranch, String title, String description);

    /**
     * 清理工作区
     *
     * @param taskId 任务ID
     */
    void cleanup(Long taskId);
}
