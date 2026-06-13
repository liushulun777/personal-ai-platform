package com.platform.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Prompt模板VO
 */
@Data
@Schema(description = "Prompt模板信息")
public class PromptVO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "Prompt内容")
    private String promptText;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "状态: 1-启用, 0-禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
