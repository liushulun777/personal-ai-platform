package com.platform.knowledge.config;

import com.platform.knowledge.typehandler.VectorTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knowledge Service MyBatis 配置 - 注册 VectorTypeHandler
 */
@Configuration
public class KnowledgeMybatisConfig {

    /**
     * 注册 VectorTypeHandler 到 MyBatis 全局配置
     * 通过 BeanPostProcessor 方式，在 MyBatis 配置初始化后注册 TypeHandler
     */
    @Bean
    public VectorTypeHandler vectorTypeHandler() {
        return new VectorTypeHandler();
    }
}
