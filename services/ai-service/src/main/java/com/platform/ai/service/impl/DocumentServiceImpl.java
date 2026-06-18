package com.platform.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.ai.convert.DocumentConvert;
import com.platform.ai.domain.dto.DocumentQueryDTO;
import com.platform.ai.domain.dto.DocumentUploadDTO;
import com.platform.ai.domain.entity.Chunk;
import com.platform.ai.domain.entity.Document;
import com.platform.ai.domain.entity.Embedding;
import com.platform.ai.domain.vo.DocumentDetailVO;
import com.platform.ai.domain.vo.DocumentVO;
import com.platform.ai.mapper.ChunkMapper;
import com.platform.ai.mapper.DocumentMapper;
import com.platform.ai.mapper.EmbeddingMapper;
import com.platform.ai.service.DocumentProcessingService;
import com.platform.ai.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;
    private final ChunkMapper chunkMapper;
    private final EmbeddingMapper embeddingMapper;
    private final DocumentConvert documentConvert;
    private final DocumentProcessingService documentProcessingService;

    @Override
    public PageResult<DocumentVO> page(DocumentQueryDTO queryDTO) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Document::getTitle, queryDTO.getTitle());
        wrapper.eq(StringUtils.hasText(queryDTO.getFileType()), Document::getFileType, queryDTO.getFileType());
        wrapper.eq(queryDTO.getStatus() != null, Document::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getCategoryId() != null, Document::getCategoryId, queryDTO.getCategoryId());
        wrapper.orderByDesc(Document::getCreateTime);

        Page<Document> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Document> result = documentMapper.selectPage(page, wrapper);

        List<DocumentVO> records = documentConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public DocumentDetailVO getById(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文档不存在");
        }
        return documentConvert.entityToDetailVO(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upload(DocumentUploadDTO dto, MultipartFile file) {
        // 1. 提取文件内容
        String fileType = getFileType(file.getOriginalFilename());
        String content = extractContent(file, fileType);

        // 2. 保存文档元数据
        Document document = new Document();
        document.setTitle(dto.getTitle());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setContent(content);
        document.setChunkCount(0);
        document.setStatus(0); // 待处理
        document.setCategoryId(dto.getCategoryId());
        document.setAuthorId(SecurityUtils.getCurrentUserIdOrNull());
        documentMapper.insert(document);

        // 3. 异步处理文档（分块 + 嵌入）
        int chunkSize = dto.getChunkSize() != null ? dto.getChunkSize() : 500;
        int chunkOverlap = dto.getChunkOverlap() != null ? dto.getChunkOverlap() : 50;
        documentProcessingService.processDocument(document.getId(), chunkSize, chunkOverlap);

        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文档不存在");
        }

        // 删除关联的嵌入
        embeddingMapper.delete(new LambdaQueryWrapper<Embedding>()
                .eq(Embedding::getDocumentId, id));
        // 删除关联的分块
        chunkMapper.delete(new LambdaQueryWrapper<Chunk>()
                .eq(Chunk::getDocumentId, id));
        // 删除文档
        documentMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reprocess(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文档不存在");
        }

        // 清理旧数据
        embeddingMapper.delete(new LambdaQueryWrapper<Embedding>()
                .eq(Embedding::getDocumentId, id));
        chunkMapper.delete(new LambdaQueryWrapper<Chunk>()
                .eq(Chunk::getDocumentId, id));

        // 重新处理
        document.setChunkCount(0);
        document.setStatus(0);
        documentMapper.updateById(document);

        documentProcessingService.processDocument(id, 500, 50);
    }

    /**
     * 根据文件名获取文件类型
     */
    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) return "unknown";
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * 提取文件文本内容
     */
    private String extractContent(MultipartFile file, String fileType) {
        try {
            return switch (fileType) {
                case "pdf" -> extractPdfContent(file);
                case "docx" -> extractDocxContent(file);
                case "md", "txt" -> extractTextContent(file);
                default -> extractTextContent(file);
            };
        } catch (Exception e) {
            log.error("提取文件内容失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException(ResultCode.FAIL, "提取文件内容失败: " + e.getMessage());
        }
    }

    private String extractPdfContent(MultipartFile file) throws Exception {
        try (PDDocument pdDocument = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdDocument);
        }
    }

    private String extractDocxContent(MultipartFile file) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            return doc.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.joining("\n"));
        }
    }

    private String extractTextContent(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
