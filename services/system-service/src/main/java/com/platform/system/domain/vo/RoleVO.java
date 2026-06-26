package com.platform.system.domain.vo;

import lombok.Data;

/**
 * 角色VO
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 数据范围: 1-全部数据, 2-仅本人数据
     */
    private Integer dataScope;
}
