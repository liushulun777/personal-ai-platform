package com.platform.mcp.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP 资源 VO
 */
@Data
@Schema(description = "MCP资源响应")
public class McpResourceVO {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "关联MCP服务ID")
    private Long serverId;

    @Schema(description = "资源URI")
    private String uri;

    @Schema(description = "资源名称")
    private String name;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "状态")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
