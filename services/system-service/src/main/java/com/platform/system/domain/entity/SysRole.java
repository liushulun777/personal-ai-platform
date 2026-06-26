package com.platform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;

    /**
     * 数据范围: 1-全部数据, 2-仅本人数据
     */
    private Integer dataScope;
}
