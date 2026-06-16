package com.platform.mcp.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP 工具 VO
 */
@Data
@Schema(description = "MCP工具响应")
public class McpToolVO {

    @Schema(description = "工具ID")
    private Long id;

    @Schema(description = "关联MCP服务ID")
    private Long serverId;

    @Schema(description = "服务名称")
    private String serverName;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "输入参数JSON Schema")
    private String inputSchema;

    @Schema(description = "输出参数JSON Schema")
    private String outputSchema;

    @Schema(description = "状态")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
