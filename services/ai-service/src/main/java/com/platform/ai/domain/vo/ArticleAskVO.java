package com.platform.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章问答响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章问答响应")
public class ArticleAskVO {

    @Schema(description = "回答内容")
    private String answer;

    @Schema(description = "Token消耗量")
    private Integer tokenCount;
}
