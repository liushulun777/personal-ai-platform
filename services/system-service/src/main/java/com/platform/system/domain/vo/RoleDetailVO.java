package com.platform.system.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 角色详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleDetailVO extends RoleVO {

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 菜单ID列表
     */
    private List<Long> menuIds;
}
