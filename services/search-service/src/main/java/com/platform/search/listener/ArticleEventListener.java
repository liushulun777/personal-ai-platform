package com.platform.search.listener;

import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.service.ArticleChunkService;
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
 * 监听 Kafka 中的文章事件，同步到 ElasticSearch 和 pgvector
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventListener {

    private final SearchService searchService;
    private final ArticleChunkService articleChunkService;

    /**
     * 监听文章发布事件
     */
    @KafkaListener(
            topics = "article-publish",
            groupId = "search-service",
            containerFactory = "articleEventListenerContainerFactory"
    )
    public void handleArticlePublish(
            @Payload(required = false) ArticleDocument document,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        if (document == null || document.getId() == null) {
            log.warn("收到空消息，跳过. topic: {}, partition: {}, offset: {}", topic, partition, offset);
            acknowledgment.acknowledge();
            return;
        }
        try {
            log.info("收到文章发布事件, topic: {}, partition: {}, offset: {}, articleId: {}",
                    topic, partition, offset, document.getId());

            // 1. 同步到 ES
            searchService.indexArticle(document);

            // 2. 分块 + 向量嵌入 + 存储到 pgvector
            articleChunkService.processArticle(document);

            acknowledgment.acknowledge();
            log.info("文章发布处理成功(ES+向量): {}", document.getId());
        } catch (Exception e) {
            log.error("处理文章发布事件失败: {}", document.getId(), e);
        }
    }

    /**
     * 监听文章更新事件
     */
    @KafkaListener(
            topics = "article-update",
            groupId = "search-service",
            containerFactory = "articleEventListenerContainerFactory"
    )
    public void handleArticleUpdate(
            @Payload(required = false) ArticleDocument document,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        if (document == null || document.getId() == null) {
            log.warn("收到空消息，跳过. topic: {}, partition: {}, offset: {}", topic, partition, offset);
            acknowledgment.acknowledge();
            return;
        }
        try {
            log.info("收到文章更新事件, topic: {}, partition: {}, offset: {}, articleId: {}",
                    topic, partition, offset, document.getId());

            // 1. 更新 ES 索引
            searchService.updateArticle(document);

            // 2. 重新分块 + 向量嵌入（内部会先删旧分块再创建新分块）
            articleChunkService.processArticle(document);

            acknowledgment.acknowledge();
            log.info("文章更新处理成功(ES+向量): {}", document.getId());
        } catch (Exception e) {
            log.error("处理文章更新事件失败: {}", document.getId(), e);
        }
    }

    /**
     * 监听文章删除事件
     */
    @KafkaListener(
            topics = "article-delete",
            groupId = "search-service",
            containerFactory = "articleDeleteListenerContainerFactory"
    )
    public void handleArticleDelete(
            @Payload(required = false) Long articleId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        if (articleId == null) {
            log.warn("收到空消息，跳过. topic: {}, partition: {}, offset: {}", topic, partition, offset);
            acknowledgment.acknowledge();
            return;
        }
        try {
            log.info("收到文章删除事件, topic: {}, partition: {}, offset: {}, articleId: {}",
                    topic, partition, offset, articleId);

            // 1. 删除 ES 索引
            searchService.deleteArticle(articleId);

            // 2. 删除向量分块
            articleChunkService.deleteByArticleId(articleId);

            acknowledgment.acknowledge();
            log.info("文章删除成功(ES+向量): {}", articleId);
        } catch (Exception e) {
            log.error("处理文章删除事件失败: {}", articleId, e);
        }
    }
}
