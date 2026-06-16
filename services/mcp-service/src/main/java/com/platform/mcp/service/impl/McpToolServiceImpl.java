package com.platform.mcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.mcp.convert.McpToolConvert;
import com.platform.mcp.domain.dto.McpToolCreateDTO;
import com.platform.mcp.domain.dto.McpToolInvokeDTO;
import com.platform.mcp.domain.dto.McpToolQueryDTO;
import com.platform.mcp.domain.entity.McpServer;
import com.platform.mcp.domain.entity.McpTool;
import com.platform.mcp.domain.vo.McpToolVO;
import com.platform.mcp.mapper.McpServerMapper;
import com.platform.mcp.mapper.McpToolMapper;
import com.platform.mcp.service.McpToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolServiceImpl implements McpToolService {

    private final McpToolMapper mcpToolMapper;
    private final McpServerMapper mcpServerMapper;
    private final McpToolConvert mcpToolConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(McpToolCreateDTO dto) {
        // 检查服务是否存在
        McpServer server = mcpServerMapper.selectById(dto.getServerId());
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }

        McpTool tool = mcpToolConvert.createDTOToEntity(dto);
        tool.setStatus(CommonConstant.STATUS_ENABLED);
        mcpToolMapper.insert(tool);
        return tool.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        McpTool tool = mcpToolMapper.selectById(id);
        if (tool == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工具不存在");
        }
        mcpToolMapper.deleteById(id);
    }

    @Override
    public McpToolVO getById(Long id) {
        McpTool tool = mcpToolMapper.selectById(id);
        if (tool == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工具不存在");
        }

        McpToolVO vo = mcpToolConvert.entityToVO(tool);

        // 填充服务名称
        McpServer server = mcpServerMapper.selectById(tool.getServerId());
        if (server != null) {
            vo.setServerName(server.getName());
        }

        return vo;
    }

    @Override
    public PageResult<McpToolVO> page(McpToolQueryDTO queryDTO) {
        LambdaQueryWrapper<McpTool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getServerId() != null, McpTool::getServerId, queryDTO.getServerId());
        wrapper.like(StringUtils.hasText(queryDTO.getName()), McpTool::getName, queryDTO.getName());
        wrapper.eq(queryDTO.getStatus() != null, McpTool::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(McpTool::getCreateTime);

        Page<McpTool> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<McpTool> result = mcpToolMapper.selectPage(page, wrapper);

        List<McpToolVO> records = mcpToolConvert.entityListToVOList(result.getRecords());

        // 填充服务名称
        for (McpToolVO vo : records) {
            McpServer server = mcpServerMapper.selectById(vo.getServerId());
            if (server != null) {
                vo.setServerName(server.getName());
            }
        }

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Map<String, Object> invoke(McpToolInvokeDTO dto) {
        McpTool tool = mcpToolMapper.selectById(dto.getToolId());
        if (tool == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工具不存在");
        }

        if (tool.getStatus() != CommonConstant.STATUS_ENABLED) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "工具已禁用");
        }

        McpServer server = mcpServerMapper.selectById(tool.getServerId());
        if (server == null || server.getStatus() != CommonConstant.STATUS_ENABLED) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "MCP服务未启用");
        }

        // TODO: 根据 transportType 调用实际的 MCP 协议
        // stdio: 启动进程，发送 JSON-RPC 请求
        // sse: 连接 SSE 端点，发送请求
        // streamable_http: 发送 HTTP 请求
        log.info("调用 MCP 工具: server={}, tool={}, args={}", server.getName(), tool.getName(), dto.getArguments());

        return Map.of(
                "status", "success",
                "tool", tool.getName(),
                "server", server.getName(),
                "message", "工具调用成功（MCP协议调用待实现）"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromServer(Long serverId) {
        McpServer server = mcpServerMapper.selectById(serverId);
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }

        // TODO: 根据 transportType 连接 MCP Server，获取工具列表
        // 1. stdio: 启动进程，发送 tools/list 请求
        // 2. sse: 连接 SSE 端点，发送 tools/list 请求
        // 3. streamable_http: 发送 HTTP POST 到 /mcp 端点
        log.info("同步 MCP 工具列表: server={}, transport={}", server.getName(), server.getTransportType());

        // 清除该服务下已有的工具
        mcpToolMapper.delete(new LambdaQueryWrapper<McpTool>()
                .eq(McpTool::getServerId, serverId));

        // TODO: 解析 MCP Server 返回的工具列表并保存
        log.info("MCP 工具同步完成（实际同步逻辑待实现）");
    }
}
