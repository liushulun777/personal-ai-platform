package com.platform.mcp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.mcp.domain.entity.McpServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * MCP 服务 Mapper
 */
@Mapper
public interface McpServerMapper extends BaseMapper<McpServer> {
}
