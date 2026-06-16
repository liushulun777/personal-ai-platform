package com.platform.mcp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * MCP 平台服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.platform.mcp.mapper")
@ComponentScan(basePackages = {"com.platform.mcp", "com.platform.common"})
public class McpApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}
