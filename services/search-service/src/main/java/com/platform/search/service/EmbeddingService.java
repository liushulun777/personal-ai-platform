package com.platform.search.service;

import java.util.List;

/**
 * 向量嵌入服务接口
 */
public interface EmbeddingService {

    /**
     * 将文本转换为向量嵌入
     *
     * @param text 输入文本
     * @return 嵌入向量
     */
    float[] embed(String text);

    /**
     * 批量将文本转换为向量嵌入
     *
     * @param texts 输入文本列表
     * @return 嵌入向量列表
     */
    List<float[]> embedBatch(List<String> texts);
}
