package com.platform.project.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BugVO
 */
@Data
public class BugVO {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Integer severity;
    private Integer status;
    private Long assigneeId;
    private Long reporterId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
