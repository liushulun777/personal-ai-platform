package com.platform.search.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 配置
 * 文章事件接收原始 JSON 字符串，在 listener 中手动解析（避免 Jackson 长度限制）
 */
@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:search-service}")
    private String groupId;

    /**
     * 默认容器工厂：反序列化为 String（原始 JSON）
     * 用于 article-publish / article-update topic
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(skipDeserializationErrorHandler());
        return factory;
    }

    /**
     * 文章删除容器工厂：反序列化为 Long（文章ID）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Long> articleDeleteListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Long> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(longConsumerFactory());
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(skipDeserializationErrorHandler());
        return factory;
    }

    private ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private ConsumerFactory<String, Long> longConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, LongDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private DefaultErrorHandler skipDeserializationErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.warn("跳过反序列化失败的消息, topic: {}, partition: {}, offset: {}, error: {}",
                            record.topic(), record.partition(), record.offset(), exception.getMessage());
                },
                new FixedBackOff(0L, 0L)
        );
        errorHandler.addNotRetryableExceptions(
                DeserializationException.class,
                org.apache.kafka.common.errors.SerializationException.class
        );
        return errorHandler;
    }

    private Map<String, Object> baseProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }
}
