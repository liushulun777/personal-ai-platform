package com.platform.ai.service;

import com.platform.common.core.result.PageResult;
import com.platform.ai.domain.dto.DocumentQueryDTO;
import com.platform.ai.domain.dto.DocumentUploadDTO;
import com.platform.ai.domain.vo.DocumentDetailVO;
import com.platform.ai.domain.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档服务接口
 */
public interface DocumentService {

    /**
     * 分页查询文档
     */
    PageResult<DocumentVO> page(DocumentQueryDTO queryDTO);

    /**
     * 获取文档详情
     */
    DocumentDetailVO getById(Long id);

    /**
     * 上传文档
     */
    Long upload(DocumentUploadDTO dto, MultipartFile file);

    /**
     * 删除文档
     */
    void delete(Long id);

    /**
     * 重新处理文档
     */
    void reprocess(Long id);
}
