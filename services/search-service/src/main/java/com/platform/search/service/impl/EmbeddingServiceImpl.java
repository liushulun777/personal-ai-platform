package com.platform.search.service.impl;

import com.platform.search.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 向量嵌入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] embed(String text) {
        try {
            float[] embedding = embeddingModel.embed(text);
            log.debug("生成嵌入向量，维度: {}", embedding.length);
            return embedding;
        } catch (Exception e) {
            log.error("生成嵌入向量失败", e);
            throw new RuntimeException("生成嵌入向量失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        try {
            List<float[]> results = embeddingModel.embed(texts);
            log.debug("批量生成嵌入向量，数量: {}", results.size());
            return results;
        } catch (Exception e) {
            log.error("批量生成嵌入向量失败", e);
            throw new RuntimeException("批量生成嵌入向量失败: " + e.getMessage(), e);
        }
    }
}
