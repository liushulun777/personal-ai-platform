package com.platform.mcp.service;

import com.platform.common.core.result.PageResult;
import com.platform.mcp.domain.dto.McpServerCreateDTO;
import com.platform.mcp.domain.dto.McpServerQueryDTO;
import com.platform.mcp.domain.dto.McpServerUpdateDTO;
import com.platform.mcp.domain.vo.McpServerDetailVO;
import com.platform.mcp.domain.vo.McpServerVO;

/**
 * MCP 服务接口
 */
public interface McpServerService {

    /**
     * 创建 MCP 服务
     */
    Long create(McpServerCreateDTO dto);

    /**
     * 更新 MCP 服务
     */
    void update(Long id, McpServerUpdateDTO dto);

    /**
     * 删除 MCP 服务
     */
    void delete(Long id);

    /**
     * 获取服务详情（含工具和资源列表）
     */
    McpServerDetailVO getById(Long id);

    /**
     * 分页查询服务
     */
    PageResult<McpServerVO> page(McpServerQueryDTO queryDTO);

    /**
     * 启用服务
     */
    void enable(Long id);

    /**
     * 禁用服务
     */
    void disable(Long id);
}
