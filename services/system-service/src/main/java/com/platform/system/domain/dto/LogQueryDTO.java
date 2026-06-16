package com.platform.system.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogQueryDTO extends PageQuery {

    /**
     * 模块
     */
    private String module;

    /**
     * 操作
     */
    private String operation;

    /**
     * 状态: 1-成功, 0-失败
     */
    private Integer status;

    /**
     * 用户名
     */
    private String username;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
