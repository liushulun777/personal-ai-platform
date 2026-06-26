package com.platform.search.listener;

import com.platform.search.convert.ArticleEventConvert;
import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.service.ArticleChunkService;
import com.platform.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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
    private final ArticleEventConvert articleEventConvert;

    /**
     * 监听文章发布事件
     * 接收原始 JSON 字符串，避免 Jackson 自动反序列化的长度限制
     */
    @KafkaListener(
            topics = "article-publish",
            groupId = "search-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleArticlePublish(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            ArticleDocument document = articleEventConvert.parseToDocument(payload);
            if (document == null || document.getId() == null) {
                log.warn("收到空消息，跳过. topic: {}, offset: {}", topic, offset);
                return;
            }

            log.info("收到文章发布事件, articleId: {}, topic: {}, offset: {}", document.getId(), topic, offset);

            searchService.indexArticle(document);
            articleChunkService.processArticle(document);

            log.info("文章发布处理成功: {}", document.getId());
        } catch (Exception e) {
            log.error("处理文章发布事件失败", e);
        }
    }

    /**
     * 监听文章更新事件
     */
    @KafkaListener(
            topics = "article-update",
            groupId = "search-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleArticleUpdate(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            ArticleDocument document = articleEventConvert.parseToDocument(payload);
            if (document == null || document.getId() == null) {
                log.warn("收到空消息，跳过. topic: {}, offset: {}", topic, offset);
                return;
            }

            log.info("收到文章更新事件, articleId: {}, topic: {}, offset: {}", document.getId(), topic, offset);

            searchService.updateArticle(document);
            articleChunkService.processArticle(document);

            log.info("文章更新处理成功: {}", document.getId());
        } catch (Exception e) {
            log.error("处理文章更新事件失败", e);
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
            @Payload Long articleId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        if (articleId == null) {
            log.warn("收到空消息，跳过. topic: {}, offset: {}", topic, offset);
            return;
        }
        try {
            log.info("收到文章删除事件, articleId: {}, topic: {}, offset: {}", articleId, topic, offset);

            searchService.deleteArticle(articleId);
            articleChunkService.deleteByArticleId(articleId);

            log.info("文章删除成功: {}", articleId);
        } catch (Exception e) {
            log.error("处理文章删除事件失败: {}", articleId, e);
        }
    }
}
