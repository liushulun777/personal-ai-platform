package com.platform.mcp.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * MCP 工具创建 DTO
 */
@Data
@Schema(description = "MCP工具创建请求")
public class McpToolCreateDTO {

    @NotNull(message = "服务ID不能为空")
    @Schema(description = "关联MCP服务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serverId;

    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "输入参数JSON Schema")
    private String inputSchema;

    @Schema(description = "输出参数JSON Schema")
    private String outputSchema;
}
