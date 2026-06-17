package com.platform.search.config;

import com.platform.search.domain.entity.ArticleDocument;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 配置
 * <p>
 * 为不同 topic 创建独立的 ListenerContainerFactory，解决跨服务 JSON 反序列化类型不匹配问题。
 * 使用 ErrorHandlingDeserializer 包装，遇到旧消息（带类型头）反序列化失败时自动跳过。
 */
@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:search-service}")
    private String groupId;

    /**
     * 文章事件容器工厂（publish / update topic）
     * 反序列化为 ArticleDocument
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ArticleDocument> articleEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ArticleDocument> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(articleEventConsumerFactory());
        factory.setConcurrency(3);
        // 跳过反序列化失败的消息（旧消息带类型头）
        factory.setCommonErrorHandler(skipDeserializationErrorHandler());
        return factory;
    }

    /**
     * 文章删除容器工厂（delete topic）
     * 反序列化为 Long（文章ID）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Long> articleDeleteListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Long> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(articleDeleteConsumerFactory());
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(skipDeserializationErrorHandler());
        return factory;
    }

    private ConsumerFactory<String, ArticleDocument> articleEventConsumerFactory() {
        Map<String, Object> props = baseProps();
        // 用 ErrorHandlingDeserializer 包装，反序列化失败时不抛异常，返回 null
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ArticleDocument.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private ConsumerFactory<String, Long> articleDeleteConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, LongDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 跳过反序列化失败的消息，不阻塞消费
     */
    private DefaultErrorHandler skipDeserializationErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.warn("跳过反序列化失败的消息, topic: {}, partition: {}, offset: {}, error: {}",
                            record.topic(), record.partition(), record.offset(), exception.getMessage());
                },
                new FixedBackOff(0L, 0L) // 不重试，直接跳过
        );
        // 关键：添加 DeserializationException 到可处理的异常列表
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
