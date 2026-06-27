package com.platform.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体（追加型表，不继承BaseEntity）
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String responseData;
    private String ip;
    private String userAgent;
    private Long userId;
    private String username;
    private Integer status;
    private String errorMsg;
    private Long duration;
    private LocalDateTime createTime;
}
