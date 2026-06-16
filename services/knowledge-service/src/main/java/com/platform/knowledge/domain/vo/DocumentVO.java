package com.platform.knowledge.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档列表VO
 */
@Data
public class DocumentVO {

    private Long id;
    private String title;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private Integer status;
    private Long categoryId;
    private Long authorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
