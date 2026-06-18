package com.platform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notification")
public class Notification extends BaseEntity {

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 发送者ID（系统消息为0）
     */
    private Long senderId;

    /**
     * 通知类型：SYSTEM-系统消息, TASK-任务通知, COMMENT-评论通知
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 是否已读：0-未读, 1-已读
     */
    private Integer readStatus;
}
