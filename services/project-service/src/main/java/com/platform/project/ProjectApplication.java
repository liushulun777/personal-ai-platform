package com.platform.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 项目管理服务启动类
 * 排除 Spring AI OpenAI 自动配置（使用自定义 ChatModelFactory）
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@EnableDiscoveryClient
@MapperScan("com.platform.project.mapper")
@ComponentScan(basePackages = {"com.platform.project", "com.platform.common"})
public class ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }
}
