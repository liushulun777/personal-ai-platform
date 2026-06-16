package com.platform.ai.feign.dto;

import lombok.Data;

/**
 * 文章内容DTO（用于 Feign 跨服务调用）
 */
@Data
public class ArticleContentDTO {

    private Long id;
    private String title;
    private String content;
}
