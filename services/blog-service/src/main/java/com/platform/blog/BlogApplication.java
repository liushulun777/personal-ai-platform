package com.platform.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 博客服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.platform.blog.mapper")
@ComponentScan(basePackages = {"com.platform.blog", "com.platform.common"})
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
