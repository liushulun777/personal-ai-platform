package com.platform.knowledge.service;

import java.util.List;

/**
 * 文本分块服务接口
 */
public interface ChunkingService {

    /**
     * 将文本分块
     *
     * @param content    原始文本
     * @param chunkSize  分块大小（字符数）
     * @param overlap    分块重叠（字符数）
     * @return 分块列表
     */
    List<String> chunk(String content, int chunkSize, int overlap);
}
