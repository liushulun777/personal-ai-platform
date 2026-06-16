package com.platform.mcp.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * MCP 服务更新 DTO
 */
@Data
@Schema(description = "MCP服务更新请求")
public class McpServerUpdateDTO {

    @Schema(description = "服务名称")
    private String name;

    @Schema(description = "服务描述")
    private String description;

    @Schema(description = "传输类型: stdio, sse, streamable_http")
    private String transportType;

    @Schema(description = "SSE/HTTP端点地址")
    private String endpoint;

    @Schema(description = "stdio启动命令")
    private String command;

    @Schema(description = "stdio启动参数(JSON数组)")
    private String args;

    @Schema(description = "环境变量(JSON对象)")
    private String envVars;
}
