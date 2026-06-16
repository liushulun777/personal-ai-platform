package com.platform.mcp.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * MCP 工具调用 DTO
 */
@Data
@Schema(description = "MCP工具调用请求")
public class McpToolInvokeDTO {

    @NotNull(message = "工具ID不能为空")
    @Schema(description = "工具ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toolId;

    @Schema(description = "调用参数")
    private Map<String, Object> arguments;
}
