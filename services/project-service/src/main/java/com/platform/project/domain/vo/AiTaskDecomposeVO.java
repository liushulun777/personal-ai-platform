package com.platform.project.domain.vo;

import lombok.Data;

/**
 * AI任务拆解结果VO
 */
@Data
public class AiTaskDecomposeVO {

    /**
     * 任务标题
     */
    private String title;

    /**
     * 优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT
     */
    private Integer priority;

    /**
     * 任务描述
     */
    private String description;
}
