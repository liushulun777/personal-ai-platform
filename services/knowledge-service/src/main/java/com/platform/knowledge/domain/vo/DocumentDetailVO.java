package com.platform.knowledge.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentDetailVO extends DocumentVO {

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 提取的文本内容
     */
    private String content;
}
