package com.platform.mcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.mcp.convert.McpResourceConvert;
import com.platform.mcp.domain.entity.McpResource;
import com.platform.mcp.domain.vo.McpResourceVO;
import com.platform.mcp.mapper.McpResourceMapper;
import com.platform.mcp.service.McpResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MCP 资源实现
 */
@Service
@RequiredArgsConstructor
public class McpResourceServiceImpl implements McpResourceService {

    private final McpResourceMapper mcpResourceMapper;
    private final McpResourceConvert mcpResourceConvert;

    @Override
    public List<McpResourceVO> listByServerId(Long serverId) {
        List<McpResource> resources = mcpResourceMapper.selectList(
                new LambdaQueryWrapper<McpResource>()
                        .eq(McpResource::getServerId, serverId)
                        .eq(McpResource::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByDesc(McpResource::getCreateTime)
        );
        return mcpResourceConvert.entityListToVOList(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        McpResource resource = mcpResourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "资源不存在");
        }
        mcpResourceMapper.deleteById(id);
    }
}
