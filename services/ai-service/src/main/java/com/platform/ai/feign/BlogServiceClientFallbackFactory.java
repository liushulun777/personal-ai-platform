package com.platform.ai.feign;

import com.platform.ai.feign.dto.ArticleContentDTO;
import com.platform.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * BlogServiceClient 降级工厂
 */
@Slf4j
@Component
public class BlogServiceClientFallbackFactory implements FallbackFactory<BlogServiceClient> {

    @Override
    public BlogServiceClient create(Throwable cause) {
        return new BlogServiceClient() {
            @Override
            public Result<ArticleContentDTO> getArticleById(Long id) {
                log.error("获取文章内容失败，文章ID: {}, 原因: {}", id, cause.getMessage());
                ArticleContentDTO emptyArticle = new ArticleContentDTO();
                emptyArticle.setId(id);
                emptyArticle.setTitle("文章内容获取失败");
                emptyArticle.setContent("文章内容暂时不可用，请稍后重试");
                return Result.success(emptyArticle);
            }
        };
    }
}