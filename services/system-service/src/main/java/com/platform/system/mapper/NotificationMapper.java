package com.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.system.domain.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 获取未读消息数量
     */
    @Select("SELECT COUNT(*) FROM sys_notification WHERE user_id = #{userId} AND read_status = 0 AND deleted = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 标记用户所有消息为已读
     */
    @Update("UPDATE sys_notification SET read_status = 1 WHERE user_id = #{userId} AND read_status = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
