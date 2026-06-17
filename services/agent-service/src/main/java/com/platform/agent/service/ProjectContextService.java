package com.platform.agent.service;

/**
 * 项目上下文服务 - 读取项目结构和技术栈
 */
public interface ProjectContextService {

    /**
     * 获取项目代码结构
     * @param projectId 项目ID
     * @return 项目结构描述
     */
    String getProjectStructure(Long projectId);

    /**
     * 获取项目技术栈
     * @param projectId 项目ID
     * @return 技术栈描述
     */
    String getProjectTechStack(Long projectId);

    /**
     * 获取项目完整上下文（结构+技术栈+关键代码）
     * @param projectId 项目ID
     * @return 完整上下文
     */
    String getFullContext(Long projectId);

    /**
     * 读取工作区目录结构
     * @param workspacePath 工作区路径
     * @param maxDepth 最大深度
     * @return 目录结构字符串
     */
    String readDirectoryStructure(String workspacePath, int maxDepth);
}
