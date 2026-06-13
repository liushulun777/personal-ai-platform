package com.platform.blog.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 文章事件发布者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发布文章发布事件
     *
     * @param event 文章事件
     */
    public void publishArticlePublishEvent(ArticleEvent event) {
        event.setEventType("PUBLISH");
        kafkaTemplate.send("article-publish", event.getArticleId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("文章发布事件发送成功: {}", event.getArticleId());
                    } else {
                        log.error("文章发布事件发送失败: {}", event.getArticleId(), ex);
                    }
                });
    }

    /**
     * 发布文章更新事件
     *
     * @param event 文章事件
     */
    public void publishArticleUpdateEvent(ArticleEvent event) {
        event.setEventType("UPDATE");
        kafkaTemplate.send("article-update", event.getArticleId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("文章更新事件发送成功: {}", event.getArticleId());
                    } else {
                        log.error("文章更新事件发送失败: {}", event.getArticleId(), ex);
                    }
                });
    }

    /**
     * 发布文章删除事件
     *
     * @param articleId 文章ID
     */
    public void publishArticleDeleteEvent(Long articleId) {
        kafkaTemplate.send("article-delete", articleId.toString(), articleId)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("文章删除事件发送成功: {}", articleId);
                    } else {
                        log.error("文章删除事件发送失败: {}", articleId, ex);
                    }
                });
    }
}
