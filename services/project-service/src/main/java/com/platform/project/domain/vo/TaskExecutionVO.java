package com.platform.project.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行记录VO
 */
@Data
public class TaskExecutionVO {

    private Long id;
    private Long taskId;
    private String executorType;
    private Long executorId;
    private String action;
    private String content;
    private String prompt;
    private String result;
    private Integer status;
    private String errorMsg;
    private Long duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
