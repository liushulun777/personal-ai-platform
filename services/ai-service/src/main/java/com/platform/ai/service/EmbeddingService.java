package com.platform.ai.service;

import java.util.List;

/**
 * 向量嵌入服务接口
 */
public interface EmbeddingService {

    /**
     * 生成单个文本的嵌入向量
     *
     * @param text 文本
     * @return 嵌入向量
     */
    float[] embed(String text);

    /**
     * 批量生成嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<float[]> embedBatch(List<String> texts);
}
