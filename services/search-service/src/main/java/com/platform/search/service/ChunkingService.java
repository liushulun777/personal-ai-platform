package com.platform.search.service;

import java.util.List;

/**
 * 文本分块服务接口
 */
public interface ChunkingService {

    /**
     * 将文本分割为多个块
     *
     * @param content    原始文本
     * @param chunkSize  每块最大字符数
     * @param overlap    块之间的重叠字符数
     * @return 分块后的文本列表
     */
    List<String> chunk(String content, int chunkSize, int overlap);
}
