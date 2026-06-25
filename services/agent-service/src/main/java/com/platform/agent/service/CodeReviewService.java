package com.platform.agent.service;

import java.util.List;
import java.util.Map;

/**
 * 代码审查服务接口
 */
public interface CodeReviewService {

    /**
     * 审查代码
     *
     * @param code     代码内容
     * @param language 编程语言
     * @param context  上下文信息
     * @return 审查结果
     */
    Map<String, Object> reviewCode(String code, String language, String context);

    /**
     * 审查文件
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @return 审查结果
     */
    Map<String, Object> reviewFile(String filePath, String content);

    /**
     * 批量审查文件
     *
     * @param files 文件列表 Map<文件路径, 文件内容>
     * @return 审查结果列表
     */
    List<Map<String, Object>> reviewFiles(Map<String, String> files);
}
