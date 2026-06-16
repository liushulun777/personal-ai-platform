package com.platform.mcp.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.mcp.domain.dto.McpServerCreateDTO;
import com.platform.mcp.domain.dto.McpServerQueryDTO;
import com.platform.mcp.domain.dto.McpServerUpdateDTO;
import com.platform.mcp.domain.vo.McpServerDetailVO;
import com.platform.mcp.domain.vo.McpServerVO;
import com.platform.mcp.service.McpServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * MCP 服务管理
 */
@Tag(name = "MCP服务管理")
@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class McpServerController {

    private final McpServerService mcpServerService;

    @Operation(summary = "注册MCP服务")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody McpServerCreateDTO dto) {
        return Result.success(mcpServerService.create(dto));
    }

    @Operation(summary = "更新MCP服务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody McpServerUpdateDTO dto) {
        mcpServerService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除MCP服务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mcpServerService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取MCP服务详情")
    @GetMapping("/{id}")
    public Result<McpServerDetailVO> getById(@PathVariable Long id) {
        return Result.success(mcpServerService.getById(id));
    }

    @Operation(summary = "分页查询MCP服务")
    @GetMapping
    public Result<PageResult<McpServerVO>> page(McpServerQueryDTO queryDTO) {
        return Result.success(mcpServerService.page(queryDTO));
    }

    @Operation(summary = "启用MCP服务")
    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        mcpServerService.enable(id);
        return Result.success();
    }

    @Operation(summary = "禁用MCP服务")
    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        mcpServerService.disable(id);
        return Result.success();
    }
}
