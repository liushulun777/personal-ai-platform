package com.platform.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文章生成请求DTO
 */
@Data
public class ArticleGenerateDTO {

    /**
     * 文章主题/标题
     */
    @NotBlank(message = "文章主题不能为空")
    private String topic;

    /**
     * 文章类型: tech_blog, tutorial, summary, essay
     */
    private String type = "tech_blog";

    /**
     * 写作风格: professional, casual, concise
     */
    private String style = "professional";

    /**
     * 文章长度: short, medium, long
     */
    private String length = "medium";

    /**
     * 补充说明
     */
    private String extra;
}
