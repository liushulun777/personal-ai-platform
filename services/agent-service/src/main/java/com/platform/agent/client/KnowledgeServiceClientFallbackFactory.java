package com.platform.agent.client;

import com.platform.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * KnowledgeServiceClient 降级工厂
 */
@Slf4j
@Component
public class KnowledgeServiceClientFallbackFactory implements FallbackFactory<KnowledgeServiceClient> {

    @Override
    public KnowledgeServiceClient create(Throwable cause) {
        return new KnowledgeServiceClient() {
            @Override
            public Result<Map<String, Object>> query(Map<String, Object> queryDTO) {
                log.error("知识库查询失败，参数: {}, 原因: {}", queryDTO, cause.getMessage());
                return Result.success(Collections.emptyMap());
            }
        };
    }
}