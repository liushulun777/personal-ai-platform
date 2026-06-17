package com.platform.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行记录实体
 */
@Data
@TableName("pm_task_execution")
public class TaskExecution {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务ID
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
