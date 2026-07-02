package com.platform.agent.client;

import com.platform.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * McpServiceClient 降级工厂
 */
@Slf4j
@Component
public class McpServiceClientFallbackFactory implements FallbackFactory<McpServiceClient> {

    @Override
    public McpServiceClient create(Throwable cause) {
        return new McpServiceClient() {
            @Override
            public Result<Map<String, Object>> invokeTool(Map<String, Object> invokeDTO) {
                log.error("调用MCP工具失败，参数: {}, 原因: {}", invokeDTO, cause.getMessage());
                return Result.success(Collections.emptyMap());
            }
        };
    }
}