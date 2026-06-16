package com.platform.knowledge.config;

import com.platform.knowledge.typehandler.VectorTypeHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Bean;

/**
 * Knowledge Service MyBatis 配置 - 注册 VectorTypeHandler
 */
@org.springframework.context.annotation.Configuration
public class KnowledgeMybatisConfig {

    @Bean
    public Configuration mybatisConfiguration() {
        Configuration configuration = new Configuration();
        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        registry.register(float[].class, new VectorTypeHandler());
        return configuration;
    }
}
