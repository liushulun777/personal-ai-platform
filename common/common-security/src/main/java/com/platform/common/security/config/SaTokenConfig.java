package com.platform.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/error",
                        // 博客公开读取接口
                        "/articles",
                        "/articles/*",
                        "/statistics",
                        "/categories",
                        "/categories/*",
                        "/tags",
                        "/tags/*",
                        "/comments/article/*",
                        "/likes/*",
                        "/likes/*/status",
                        "/favorites/*",
                        "/favorites/*/status"
                );
    }
}
