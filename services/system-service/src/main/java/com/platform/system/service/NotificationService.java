package com.platform.system.service;

import com.platform.common.core.result.PageResult;
import com.platform.system.domain.entity.Notification;

/**
 * 消息通知服务接口
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    void send(Notification notification);

    /**
     * 获取用户通知列表
     */
    PageResult<Notification> listByUserId(Long userId, Integer readStatus, Integer page, Integer size);

    /**
     * 获取未读消息数量
     */
    int countUnread(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除消息
     */
    void delete(Long id, Long userId);
}
