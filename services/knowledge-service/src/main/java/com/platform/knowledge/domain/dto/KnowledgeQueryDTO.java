package com.platform.knowledge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库RAG查询DTO
 */
@Data
public class KnowledgeQueryDTO {

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 返回的最相关分块数量
     */
    private Integer topK;

    /**
     * 分类ID过滤（可选）
     */
    private Long categoryId;
}
