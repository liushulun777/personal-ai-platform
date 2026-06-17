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
}
