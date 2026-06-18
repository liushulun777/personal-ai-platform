package com.platform.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文档上传DTO
 */
@Data
public class DocumentUploadDTO {

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分块大小（字符数）
     */
    private Integer chunkSize;

    /**
     * 分块重叠（字符数）
     */
    private Integer chunkOverlap;
}
