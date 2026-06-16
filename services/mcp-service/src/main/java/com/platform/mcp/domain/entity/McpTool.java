package com.platform.mcp.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP 工具实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mcp_tool")
public class McpTool extends BaseEntity {

    /** 关联MCP服务ID */
    private Long serverId;

    /** 工具名称 */
    private String name;

    /** 工具描述 */
    private String description;

    /** 输入参数JSON Schema */
    private String inputSchema;

    /** 输出参数JSON Schema */
    private String outputSchema;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
}
