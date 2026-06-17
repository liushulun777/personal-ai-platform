package com.platform.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * <p>
 * 使用 SaInterceptor 拦截所有请求，excludePathPatterns 排除公开路径。
 * 注意：Gateway 配置了 StripPrefix=1，实际到达服务的路径不含 /auth 前缀。
 * 公开的博客读取接口仅放行 GET 请求，POST/PUT/DELETE 仍需登录。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 公开的 GET 接口：放行
                    SaRouter.match("/articles", "/articles/*", "/statistics",
                                    "/categories", "/categories/*", "/tags", "/tags/*",
                                    "/comments/article/*", "/likes/*", "/likes/*/status",
                                    "/favorites/*", "/favorites/*/status")
                            .matchMethod("GET")
                            .stop();

                    // 其他所有接口：需要登录
                    StpUtil.checkLogin();
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证接口（Gateway 已 StripPrefix，实际路径无 /auth 前缀）
                        "/login",
                        "/register",
                        // Swagger / Knife4j
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        // 错误页
                        "/error"
                );
    }
}
