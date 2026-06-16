package com.platform.mcp.convert;

import com.platform.mcp.domain.entity.McpResource;
import com.platform.mcp.domain.vo.McpResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MCP 资源 MapStruct 转换器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface McpResourceConvert {

    McpResourceVO entityToVO(McpResource resource);

    List<McpResourceVO> entityListToVOList(List<McpResource> resources);
}
