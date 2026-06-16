package com.platform.mcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.mcp.convert.McpServerConvert;
import com.platform.mcp.domain.dto.McpServerCreateDTO;
import com.platform.mcp.domain.dto.McpServerQueryDTO;
import com.platform.mcp.domain.dto.McpServerUpdateDTO;
import com.platform.mcp.domain.entity.McpResource;
import com.platform.mcp.domain.entity.McpServer;
import com.platform.mcp.domain.entity.McpTool;
import com.platform.mcp.domain.vo.McpServerDetailVO;
import com.platform.mcp.domain.vo.McpServerVO;
import com.platform.mcp.domain.vo.McpToolVO;
import com.platform.mcp.domain.vo.McpResourceVO;
import com.platform.mcp.mapper.McpResourceMapper;
import com.platform.mcp.mapper.McpServerMapper;
import com.platform.mcp.mapper.McpToolMapper;
import com.platform.mcp.service.McpServerService;
import com.platform.mcp.convert.McpToolConvert;
import com.platform.mcp.convert.McpResourceConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MCP 服务实现
 */
@Service
@RequiredArgsConstructor
public class McpServerServiceImpl implements McpServerService {

    private final McpServerMapper mcpServerMapper;
    private final McpToolMapper mcpToolMapper;
    private final McpResourceMapper mcpResourceMapper;
    private final McpServerConvert mcpServerConvert;
    private final McpToolConvert mcpToolConvert;
    private final McpResourceConvert mcpResourceConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(McpServerCreateDTO dto) {
        McpServer server = mcpServerConvert.createDTOToEntity(dto);
        server.setStatus(CommonConstant.STATUS_ENABLED);
        server.setAuthorId(SecurityUtils.getCurrentUserIdOrNull());
        mcpServerMapper.insert(server);
        return server.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, McpServerUpdateDTO dto) {
        McpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }
        mcpServerConvert.updateEntityFromDTO(dto, server);
        mcpServerMapper.updateById(server);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        McpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }
        // 删除关联的工具
        mcpToolMapper.delete(new LambdaQueryWrapper<McpTool>()
                .eq(McpTool::getServerId, id));
        // 删除关联的资源
        mcpResourceMapper.delete(new LambdaQueryWrapper<McpResource>()
                .eq(McpResource::getServerId, id));
        // 删除服务
        mcpServerMapper.deleteById(id);
    }

    @Override
    public McpServerDetailVO getById(Long id) {
        McpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }

        McpServerDetailVO detailVO = mcpServerConvert.entityToDetailVO(server);

        // 填充工具列表
        List<McpTool> tools = mcpToolMapper.selectList(
                new LambdaQueryWrapper<McpTool>()
                        .eq(McpTool::getServerId, id)
                        .eq(McpTool::getDeleted, CommonConstant.NOT_DELETED)
        );
        detailVO.setTools(mcpToolConvert.entityListToVOList(tools));

        // 填充资源列表
        List<McpResource> resources = mcpResourceMapper.selectList(
                new LambdaQueryWrapper<McpResource>()
                        .eq(McpResource::getServerId, id)
                        .eq(McpResource::getDeleted, CommonConstant.NOT_DELETED)
        );
        detailVO.setResources(mcpResourceConvert.entityListToVOList(resources));

        return detailVO;
    }

    @Override
    public PageResult<McpServerVO> page(McpServerQueryDTO queryDTO) {
        LambdaQueryWrapper<McpServer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), McpServer::getName, queryDTO.getName());
        wrapper.eq(queryDTO.getStatus() != null, McpServer::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(McpServer::getCreateTime);

        Page<McpServer> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<McpServer> result = mcpServerMapper.selectPage(page, wrapper);

        List<McpServerVO> records = mcpServerConvert.entityListToVOList(result.getRecords());

        // 填充每个服务的工具数量
        for (McpServerVO vo : records) {
            Long toolCount = mcpToolMapper.selectCount(
                    new LambdaQueryWrapper<McpTool>()
                            .eq(McpTool::getServerId, vo.getId())
                            .eq(McpTool::getDeleted, CommonConstant.NOT_DELETED)
            );
            vo.setToolCount(toolCount.intValue());
        }

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        McpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }
        server.setStatus(CommonConstant.STATUS_ENABLED);
        mcpServerMapper.updateById(server);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        McpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP服务不存在");
        }
        server.setStatus(CommonConstant.STATUS_DISABLED);
        mcpServerMapper.updateById(server);
    }
}
