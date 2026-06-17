package com.platform.agent.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * 自动传递 Sa-Token 到下游服务
 */
@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取当前请求的 Attributes
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes instanceof ServletRequestAttributes) {
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    // 传递 Authorization Header
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        template.header("Authorization", authorization);
                    }
                }
            }
        };
    }
}
