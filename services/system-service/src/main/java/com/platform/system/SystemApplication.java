package com.platform.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 系统管理服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.platform.system.mapper")
@ComponentScan(basePackages = {"com.platform.system", "com.platform.common"})
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
