package com.platform.search.config;

import com.platform.search.typehandler.VectorTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Search Service MyBatis 配置 - 注册 VectorTypeHandler
 */
@Configuration
public class SearchMybatisConfig {

    @Bean
    public VectorTypeHandler vectorTypeHandler() {
        return new VectorTypeHandler();
    }
}
