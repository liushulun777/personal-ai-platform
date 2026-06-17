package com.platform.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 执行步骤实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_execution_step")
public class ExecutionStep {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 执行记录 ID
     */
    private Long executionId;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤序号
     */
    private Integer stepOrder;

    /**
     * 执行状态：0-待执行 1-执行中 2-成功 3-失败 4-跳过
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 详细信息
     */
    private String detail;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
