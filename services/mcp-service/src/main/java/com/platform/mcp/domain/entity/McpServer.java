package com.platform.mcp.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP 服务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mcp_server")
public class McpServer extends BaseEntity {

    /** 服务名称 */
    private String name;

    /** 服务描述 */
    private String description;

    /** 传输类型: stdio, sse, streamable_http */
    private String transportType;

    /** SSE/HTTP端点地址 */
    private String endpoint;

    /** stdio启动命令 */
    private String command;

    /** stdio启动参数(JSON数组) */
    private String args;

    /** 环境变量(JSON对象) */
    private String envVars;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 创建者ID */
    private Long authorId;
}
