package com.platform.ai.service;

/**
 * 文档处理服务接口（异步）
 */
public interface DocumentProcessingService {

    /**
     * 异步处理文档：解析 -> 分块 -> 嵌入
     *
     * @param documentId 文档ID
     * @param chunkSize  分块大小
     * @param chunkOverlap 分块重叠
     */
    void processDocument(Long documentId, int chunkSize, int chunkOverlap);
}
