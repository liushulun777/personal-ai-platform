package com.platform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型（唯一）
     */
    private String dictType;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
