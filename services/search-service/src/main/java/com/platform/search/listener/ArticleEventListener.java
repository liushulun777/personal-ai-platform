package com.platform.search.listener;

import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 文章事件监听器
 * 监听 Kafka 中的文章事件，同步到 ElasticSearch
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventListener {

    private final SearchService searchService;

    /**
     * 监听文章发布事件
     */
    @KafkaListener(topics = "article-publish", groupId = "search-service")
    public void handleArticlePublish(
            @Payload ArticleDocument document,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("收到文章发布事件, topic: {}, partition: {}, offset: {}, articleId: {}",
                    topic, partition, offset, document.getId());

            searchService.indexArticle(document);

            acknowledgment.acknowledge();
            log.info("文章索引成功: {}", document.getId());
        } catch (Exception e) {
            log.error("处理文章发布事件失败: {}", document.getId(), e);
            // 不确认消息，等待重试
        }
    }

    /**
     * 监听文章更新事件
     */
    @KafkaListener(topics = "article-update", groupId = "search-service")
    public void handleArticleUpdate(
            @Payload ArticleDocument document,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("收到文章更新事件, topic: {}, partition: {}, offset: {}, articleId: {}",
                    topic, partition, offset, document.getId());

            searchService.updateArticle(document);

            acknowledgment.acknowledge();
            log.info("文章索引更新成功: {}", document.getId());
        } catch (Exception e) {
            log.error("处理文章更新事件失败: {}", document.getId(), e);
        }
    }

    /**
     * 监听文章删除事件
     */
    @KafkaListener(topics = "article-delete", groupId = "search-service")
    public void handleArticleDelete(
            @Payload Long articleId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("收到文章删除事件, topic: {}, partition: {}, offset: {}, articleId: {}",
                    topic, partition, offset, articleId);

            searchService.deleteArticle(articleId);

            acknowledgment.acknowledge();
            log.info("文章索引删除成功: {}", articleId);
        } catch (Exception e) {
            log.error("处理文章删除事件失败: {}", articleId, e);
        }
    }
}
