package com.platform.knowledge.controller;

import com.platform.common.core.result.Result;
import com.platform.knowledge.domain.dto.KnowledgeQueryDTO;
import com.platform.knowledge.domain.vo.KnowledgeAnswerVO;
import com.platform.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库RAG查询控制器
 */
@Tag(name = "知识库问答", description = "基于知识库的RAG问答接口")
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Operation(summary = "知识库问答", description = "基于知识库内容回答用户问题")
    @PostMapping("/query")
    public Result<KnowledgeAnswerVO> query(@Valid @RequestBody KnowledgeQueryDTO queryDTO) {
        KnowledgeAnswerVO result = knowledgeService.query(queryDTO);
        return Result.success(result);
    }
}
