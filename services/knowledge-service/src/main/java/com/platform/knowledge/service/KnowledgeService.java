package com.platform.knowledge.service;

import com.platform.knowledge.domain.dto.KnowledgeQueryDTO;
import com.platform.knowledge.domain.vo.KnowledgeAnswerVO;

/**
 * 知识库RAG查询服务接口
 */
public interface KnowledgeService {

    /**
     * RAG查询：基于知识库内容回答问题
     *
     * @param queryDTO 查询参数
     * @return 回答及来源信息
     */
    KnowledgeAnswerVO query(KnowledgeQueryDTO queryDTO);
}
