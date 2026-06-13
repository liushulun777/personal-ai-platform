package com.platform.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI提示词模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_prompt")
public class Prompt extends BaseEntity {

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 提示词内容
     */
    private String promptText;

    /**
     * 分类
     */
    private String category;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
