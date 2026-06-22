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

    /**
     * 线程本地变量存储 Token（用于异步线程）
     */
    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的 Token
     */
    public static void setToken(String token) {
        TOKEN_HOLDER.set(token);
    }

    /**
     * 获取当前线程的 Token
     */
    public static String getToken() {
        return TOKEN_HOLDER.get();
    }

    /**
     * 清除当前线程的 Token
     */
    public static void clearToken() {
        TOKEN_HOLDER.remove();
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 优先从 ThreadLocal 获取 Token（异步线程）
                String token = getToken();

                // 如果 ThreadLocal 没有，从当前请求获取
                if (token == null || token.isEmpty()) {
                    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                    if (requestAttributes instanceof ServletRequestAttributes) {
                        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                        token = request.getHeader("Authorization");
                    }
                }

                // 传递 Authorization Header
                if (token != null && !token.isEmpty()) {
                    template.header("Authorization", token);
                }
            }
        };
    }
}
