package com.platform.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库RAG问答响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeAnswerVO {

    /**
     * AI生成的回答
     */
    private String answer;

    /**
     * 来源信息
     */
    private List<SourceInfo> sources;

    /**
     * 来源信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {

        /**
         * 文档ID
         */
        private Long documentId;

        /**
         * 文档标题
         */
        private String documentTitle;

        /**
         * 分块内容（截取）
         */
        private String chunkContent;

        /**
         * 相似度分数
         */
        private Double similarity;
    }
}
