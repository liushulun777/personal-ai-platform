package com.platform.mcp.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.mcp.domain.dto.McpToolCreateDTO;
import com.platform.mcp.domain.dto.McpToolInvokeDTO;
import com.platform.mcp.domain.dto.McpToolQueryDTO;
import com.platform.mcp.domain.vo.McpToolVO;
import com.platform.mcp.service.McpToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MCP 工具管理
 */
@Tag(name = "MCP工具管理")
@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class McpToolController {

    private final McpToolService mcpToolService;

    @Operation(summary = "创建工具")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody McpToolCreateDTO dto) {
        return Result.success(mcpToolService.create(dto));
    }

    @Operation(summary = "删除工具")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mcpToolService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取工具详情")
    @GetMapping("/{id}")
    public Result<McpToolVO> getById(@PathVariable Long id) {
        return Result.success(mcpToolService.getById(id));
    }

    @Operation(summary = "分页查询工具")
    @GetMapping
    public Result<PageResult<McpToolVO>> page(McpToolQueryDTO queryDTO) {
        return Result.success(mcpToolService.page(queryDTO));
    }

    @Operation(summary = "调用工具")
    @PostMapping("/invoke")
    public Result<Map<String, Object>> invoke(@Valid @RequestBody McpToolInvokeDTO dto) {
        return Result.success(mcpToolService.invoke(dto));
    }

    @Operation(summary = "从MCP Server同步工具列表")
    @PostMapping("/sync/{serverId}")
    public Result<Void> syncFromServer(@PathVariable Long serverId) {
        mcpToolService.syncFromServer(serverId);
        return Result.success();
    }
}
