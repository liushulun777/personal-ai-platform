package com.platform.file.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件VO
 */
@Data
public class FileVO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
