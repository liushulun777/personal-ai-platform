package com.platform.mcp.domain.dto;

import com.platform.common.core.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP 工具查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP工具查询请求")
public class McpToolQueryDTO extends PageQuery {

    @Schema(description = "关联MCP服务ID")
    private Long serverId;

    @Schema(description = "工具名称(模糊查询)")
    private String name;

    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;
}
