package com.platform.agent.service;

import java.util.Map;

/**
 * 代码生成服务接口
 */
public interface CodeGenerationService {

    /**
     * 生成代码
     *
     * @param context         上下文（知识库检索结果）
     * @param taskDescription 任务描述
     * @return 生成的代码 Map<文件路径, 文件内容>
     */
    Map<String, String> generateCode(String context, String taskDescription);

    /**
     * 生成代码（使用项目上下文）
     *
     * @param projectId       项目ID
     * @param taskDescription 任务描述
     * @return 生成的代码 Map<文件路径, 文件内容>
     */
    Map<String, String> generateCodeWithProjectContext(Long projectId, String taskDescription);

    /**
     * 修复代码错误
     *
     * @param context    项目上下文
     * @param codeFiles  原始代码
     * @param errorMsg   错误信息
     * @return 修复后的代码
     */
    Map<String, String> fixCodeErrors(String context, Map<String, String> codeFiles, String errorMsg);
}
