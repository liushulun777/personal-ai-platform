package com.platform.knowledge.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.platform.knowledge.typehandler.VectorTypeHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis Plus 配置
 */
@org.springframework.context.annotation.Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    @Bean
    public Configuration mybatisConfiguration() {
        Configuration configuration = new Configuration();
        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        registry.register(float[].class, new VectorTypeHandler());
        return configuration;
    }
}
