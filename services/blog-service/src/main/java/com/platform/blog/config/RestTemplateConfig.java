package com.platform.blog.config;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * RestTemplate 配置
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(tokenInterceptor()));
        return restTemplate;
    }

    /**
     * 请求拦截器 — 自动传递 Sa-Token 到下游服务
     */
    private ClientHttpRequestInterceptor tokenInterceptor() {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            try {
                String token = StpUtil.getTokenValue();
                if (token != null && !token.isEmpty()) {
                    request.getHeaders().set("Authorization", token);
                }
            } catch (Exception ignored) {
                // 未登录场景，不传递 token
            }
            return execution.execute(request, body);
        };
    }
}
