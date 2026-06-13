package com.platform.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 文件服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.platform.file.mapper")
@ComponentScan(basePackages = {"com.platform.file", "com.platform.common"})
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
