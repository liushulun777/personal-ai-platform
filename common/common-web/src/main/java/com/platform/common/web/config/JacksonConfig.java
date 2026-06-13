package com.platform.common.web.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.platform.common.web.serializer.SmartLongSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置
 * Long 类型智能序列化：超过 JS 安全整数范围的输出为字符串，其余输出为数字
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, new SmartLongSerializer());
            simpleModule.addSerializer(Long.TYPE, new SmartLongSerializer());
            builder.modulesToInstall(simpleModule);
        };
    }
}
