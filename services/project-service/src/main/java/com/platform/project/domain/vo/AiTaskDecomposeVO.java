package com.platform.project.domain.vo;

import lombok.Data;

import java.util.List;

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

    /**
     * 排序顺序（执行顺序）
     */
    private Integer sortOrder;

    /**
     * 依赖的任务序号列表（表示该任务需要在哪些任务完成后才能开始）
     */
    private List<Integer> dependencies;

    /**
     * 预估工时（小时）
     */
    private Integer estimatedHours;

    /**
     * 任务类型: design, development, testing, deployment
     */
    private String taskType;
}
