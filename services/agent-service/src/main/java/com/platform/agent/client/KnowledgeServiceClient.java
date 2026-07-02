package com.platform.agent.client;

import com.platform.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Knowledge Service Feign Client
 */
@FeignClient(name = "knowledge-service", fallbackFactory = KnowledgeServiceClientFallbackFactory.class)
public interface KnowledgeServiceClient {

    /**
     * RAG 查询
     */
    @PostMapping("/knowledge/query")
    Result<Map<String, Object>> query(@RequestBody Map<String, Object> queryDTO);
}
