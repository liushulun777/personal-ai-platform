package com.platform.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI对话实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_conversation")
public class Conversation extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
