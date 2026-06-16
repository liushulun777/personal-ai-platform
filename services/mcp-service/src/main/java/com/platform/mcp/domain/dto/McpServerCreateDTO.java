package com.platform.mcp.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * MCP 服务创建 DTO
 */
@Data
@Schema(description = "MCP服务创建请求")
public class McpServerCreateDTO {

    @NotBlank(message = "服务名称不能为空")
    @Schema(description = "服务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "服务描述")
    private String description;

    @NotBlank(message = "传输类型不能为空")
    @Schema(description = "传输类型: stdio, sse, streamable_http", requiredMode = Schema.RequiredMode.REQUIRED)
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
