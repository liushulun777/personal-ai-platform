package com.platform.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI服务启动类
 * 排除 Spring AI OpenAI 自动配置（使用自定义 ChatModelFactory）
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.platform.ai.feign")
@MapperScan({"com.platform.ai.mapper", "com.platform.common.core.mapper"})
@ComponentScan(basePackages = {"com.platform.ai", "com.platform.common"})
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
