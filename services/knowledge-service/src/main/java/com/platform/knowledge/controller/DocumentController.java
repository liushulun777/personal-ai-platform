package com.platform.knowledge.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.knowledge.domain.dto.DocumentQueryDTO;
import com.platform.knowledge.domain.dto.DocumentUploadDTO;
import com.platform.knowledge.domain.vo.DocumentDetailVO;
import com.platform.knowledge.domain.vo.DocumentVO;
import com.platform.knowledge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档管理控制器
 */
@Tag(name = "文档管理", description = "知识库文档CRUD相关接口")
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "分页查询文档")
    @GetMapping
    public Result<PageResult<DocumentVO>> page(DocumentQueryDTO queryDTO) {
        PageResult<DocumentVO> result = documentService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/{id}")
    public Result<DocumentDetailVO> getById(@PathVariable Long id) {
        DocumentDetailVO detailVO = documentService.getById(id);
        return Result.success(detailVO);
    }

    @Operation(summary = "上传文档")
    @PostMapping
    public Result<Long> upload(@RequestPart("file") MultipartFile file,
                               @Valid @RequestPart("dto") DocumentUploadDTO dto) {
        Long documentId = documentService.upload(dto, file);
        return Result.success(documentId);
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重新处理文档")
    @PostMapping("/{id}/reprocess")
    public Result<Void> reprocess(@PathVariable Long id) {
        documentService.reprocess(id);
        return Result.success();
    }
}
