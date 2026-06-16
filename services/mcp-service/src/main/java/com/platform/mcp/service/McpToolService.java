package com.platform.mcp.service;

import com.platform.common.core.result.PageResult;
import com.platform.mcp.domain.dto.McpToolCreateDTO;
import com.platform.mcp.domain.dto.McpToolInvokeDTO;
import com.platform.mcp.domain.dto.McpToolQueryDTO;
import com.platform.mcp.domain.vo.McpToolVO;

import java.util.Map;

/**
 * MCP 工具接口
 */
public interface McpToolService {

    /**
     * 创建工具
     */
    Long create(McpToolCreateDTO dto);

    /**
     * 删除工具
     */
    void delete(Long id);

    /**
     * 获取工具详情
     */
    McpToolVO getById(Long id);

    /**
     * 分页查询工具
     */
    PageResult<McpToolVO> page(McpToolQueryDTO queryDTO);

    /**
     * 调用工具
     */
    Map<String, Object> invoke(McpToolInvokeDTO dto);

    /**
     * 从 MCP Server 同步工具列表
     */
    void syncFromServer(Long serverId);
}
