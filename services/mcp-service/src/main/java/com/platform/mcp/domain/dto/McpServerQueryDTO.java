package com.platform.mcp.domain.dto;

import com.platform.common.core.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP 服务查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP服务查询请求")
public class McpServerQueryDTO extends PageQuery {

    @Schema(description = "服务名称(模糊查询)")
    private String name;

    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;
}
