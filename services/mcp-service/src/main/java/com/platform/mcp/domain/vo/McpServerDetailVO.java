package com.platform.mcp.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP 服务详情 VO
 */
@Data
@Schema(description = "MCP服务详情响应")
public class McpServerDetailVO {

    @Schema(description = "服务ID")
    private Long id;

    @Schema(description = "服务名称")
    private String name;

    @Schema(description = "服务描述")
    private String description;

    @Schema(description = "传输类型")
    private String transportType;

    @Schema(description = "SSE/HTTP端点地址")
    private String endpoint;

    @Schema(description = "stdio启动命令")
    private String command;

    @Schema(description = "stdio启动参数(JSON数组)")
    private String args;

    @Schema(description = "环境变量(JSON对象)")
    private String envVars;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建者ID")
    private Long authorId;

    @Schema(description = "关联工具列表")
    private List<McpToolVO> tools;

    @Schema(description = "关联资源列表")
    private List<McpResourceVO> resources;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
