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
            SaRouter.match("/**")
                    .notMatch(
                            "/auth/login",
                            "/auth/register",
                            "/doc.html",
                            "/webjars/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/error"
                    )
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
