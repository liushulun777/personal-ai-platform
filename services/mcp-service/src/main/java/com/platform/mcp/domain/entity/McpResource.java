package com.platform.mcp.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP 资源实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mcp_resource")
public class McpResource extends BaseEntity {

    /** 关联MCP服务ID */
    private Long serverId;

    /** 资源URI */
    private String uri;

    /** 资源名称 */
    private String name;

    /** 资源描述 */
    private String description;

    /** MIME类型 */
    private String mimeType;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
}
