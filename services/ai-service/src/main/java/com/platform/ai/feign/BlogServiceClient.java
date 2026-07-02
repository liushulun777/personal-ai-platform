package com.platform.ai.feign;

import com.platform.ai.feign.dto.ArticleContentDTO;
import com.platform.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * blog-service Feign 客户端
 */
@FeignClient(name = "blog-service", contextId = "blogServiceClient", fallbackFactory = BlogServiceClientFallbackFactory.class)
public interface BlogServiceClient {

    /**
     * 获取文章内容
     */
    @GetMapping("/articles/{id}")
    Result<ArticleContentDTO> getArticleById(@PathVariable("id") Long id);
}
