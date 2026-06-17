package com.platform.project.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务VO
 */
@Data
public class TaskVO {

    private Long id;
    private Long projectId;
    private Long parentTaskId;
    private String title;
    private String description;
    private Integer status;
    private Integer priority;
    private String sourceType;
    private Long assigneeId;
    private Long reporterId;
    private String blockedReason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
