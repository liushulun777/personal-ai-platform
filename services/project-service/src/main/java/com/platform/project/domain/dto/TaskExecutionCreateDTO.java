package com.platform.project.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务执行记录创建DTO
 */
@Data
public class TaskExecutionCreateDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /**
     * 执行者类型: AI, HUMAN, AGENT
     */
    private String executorType;

    /**
     * 执行者ID
     */
    private Long executorId;

    /**
     * 动作: CREATE, START, UPDATE, COMPLETE, COMMENT, FAIL, BLOCK, ASSIGN, REVIEW
     */
    @NotNull(message = "动作不能为空")
    private String action;

    /**
     * 执行内容
     */
    private String content;

    /**
     * AI执行的Prompt
     */
    private String prompt;

    /**
     * AI执行的结果
     */
    private String result;

    /**
     * 状态: 0-成功, 1-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行耗时(毫秒)
     */
    private Long duration;
}
