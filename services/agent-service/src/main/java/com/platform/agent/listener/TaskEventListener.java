package com.platform.agent.listener;

import com.platform.agent.domain.event.TaskCreatedEvent;
import com.platform.agent.service.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 任务事件监听器
 * 监听 Kafka 中的任务事件，触发 Agent 执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final AgentService agentService;

    /**
     * 监听任务创建事件
     */
    @KafkaListener(
            topics = "task-created",
            groupId = "agent-service",
            containerFactory = "taskEventContainerFactory"
    )
    public void handleTaskCreated(
            @Payload(required = false) TaskCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        if (event == null || event.getTaskId() == null) {
            log.warn("收到空消息，跳过. topic: {}, partition: {}, offset: {}", topic, partition, offset);
            acknowledgment.acknowledge();
            return;
        }

        try {
            log.info("收到任务创建事件, topic: {}, partition: {}, offset: {}, taskId: {}, title: {}",
                    topic, partition, offset, event.getTaskId(), event.getTitle());

            // 异步执行任务
            agentService.executeTask(event.getTaskId());

            acknowledgment.acknowledge();
            log.info("任务执行完成, taskId: {}", event.getTaskId());
        } catch (Exception e) {
            log.error("处理任务创建事件失败, taskId: {}", event.getTaskId(), e);
            // 不 ack，让消息重试
        }
    }
}
