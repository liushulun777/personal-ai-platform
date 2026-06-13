package com.platform.system.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageQuery {

    /**
     * 角色名称（模糊查询）
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
