package com.platform.report.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionQueryDTO extends PageQuery {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 资源类型
     */
    private String resourceType;
}
