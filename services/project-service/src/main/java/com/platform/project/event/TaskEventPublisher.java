package com.platform.project.event;

import com.platform.project.domain.entity.Task;
import com.platform.project.domain.event.TaskCreatedEvent;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 任务事件发布者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_TASK_CREATED = "task-created";

    /**
     * 发布任务创建事件
     *
     * @param task 任务实体
     */
    public void publishTaskCreatedEvent(Task task) {
        // 获取当前用户的 Token
        String token = null;
        try {
            token = StpUtil.getTokenValue();
        } catch (Exception e) {
            log.debug("获取 Token 失败，可能是系统内部调用");
        }

        TaskCreatedEvent event = new TaskCreatedEvent(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getSourceType(),
                token
        );

        kafkaTemplate.send(TOPIC_TASK_CREATED, task.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("发布任务创建事件成功, taskId: {}, topic: {}",
                                task.getId(), TOPIC_TASK_CREATED);
                    } else {
                        log.error("发布任务创建事件失败, taskId: {}", task.getId(), ex);
                    }
                });
    }
}
