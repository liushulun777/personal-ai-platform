package com.platform.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 执行记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_execution")
public class AgentExecution {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务 ID
     */
    private Long taskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 执行状态：0-待执行 1-执行中 2-成功 3-失败 4-超时 5-取消
     */
    private Integer status;

    /**
     * 执行器实例 ID
     */
    private String executorId;

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
     * 错误信息
     */
    private String errorMessage;

    /**
     * 结果摘要
     */
    private String resultSummary;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 执行步骤列表
     */
    @TableField(exist = false)
    private List<ExecutionStep> steps;
}
