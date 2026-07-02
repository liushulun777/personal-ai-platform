package com.platform.agent.client;

import com.platform.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * MCP Service Feign Client
 */
@FeignClient(name = "mcp-service", fallbackFactory = McpServiceClientFallbackFactory.class)
public interface McpServiceClient {

    /**
     * 调用 MCP 工具
     */
    @PostMapping("/tools/invoke")
    Result<Map<String, Object>> invokeTool(@RequestBody Map<String, Object> invokeDTO);
}
