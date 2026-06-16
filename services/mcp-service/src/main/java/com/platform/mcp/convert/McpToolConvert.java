package com.platform.mcp.convert;

import com.platform.mcp.domain.dto.McpToolCreateDTO;
import com.platform.mcp.domain.entity.McpTool;
import com.platform.mcp.domain.vo.McpToolVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MCP 工具 MapStruct 转换器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface McpToolConvert {

    McpTool createDTOToEntity(McpToolCreateDTO dto);

    McpToolVO entityToVO(McpTool tool);

    List<McpToolVO> entityListToVOList(List<McpTool> tools);
}
