package com.platform.mcp.service;

import com.platform.mcp.domain.vo.McpResourceVO;

import java.util.List;

/**
 * MCP 资源接口
 */
public interface McpResourceService {

    /**
     * 获取服务下的资源列表
     */
    List<McpResourceVO> listByServerId(Long serverId);

    /**
     * 删除资源
     */
    void delete(Long id);
}
