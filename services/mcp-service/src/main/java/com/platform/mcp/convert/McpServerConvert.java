package com.platform.mcp.convert;

import com.platform.mcp.domain.dto.McpServerCreateDTO;
import com.platform.mcp.domain.dto.McpServerUpdateDTO;
import com.platform.mcp.domain.entity.McpServer;
import com.platform.mcp.domain.vo.McpServerDetailVO;
import com.platform.mcp.domain.vo.McpServerVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MCP 服务 MapStruct 转换器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface McpServerConvert {

    McpServer createDTOToEntity(McpServerCreateDTO dto);

    McpServerVO entityToVO(McpServer server);

    McpServerDetailVO entityToDetailVO(McpServer server);

    List<McpServerVO> entityListToVOList(List<McpServer> servers);

    void updateEntityFromDTO(McpServerUpdateDTO dto, @MappingTarget McpServer server);
}
