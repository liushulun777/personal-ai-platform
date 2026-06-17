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
     * 写入文件
     *
     * @param workspacePath 工作区路径
     * @param filePath      文件路径
     * @param content       文件内容
     */
    void writeFile(String workspacePath, String filePath, String content);

    /**
     * 编译项目
     *
     * @param workspacePath 工作区路径
     * @return 是否成功
     */
    boolean compile(String workspacePath);

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
     * 清理工作区
     *
     * @param taskId 任务ID
     */
    void cleanup(Long taskId);
}
