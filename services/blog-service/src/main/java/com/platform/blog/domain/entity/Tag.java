package com.platform.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_tag")
public class Tag extends BaseEntity {

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签别名（URL友好）
     */
    private String slug;

    /**
     * 描述
     */
    private String description;

    /**
     * 颜色
     */
    private String color;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
