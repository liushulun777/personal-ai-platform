package com.platform.report.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限创建DTO
 */
@Data
public class PermissionCreateDTO {

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
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    /**
     * 资源类型: DATASOURCE/DATASET/REPORT/DASHBOARD
     */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /**
     * 权限类型: VIEW/EDIT/DELETE/EXPORT/PRINT
     */
    @NotBlank(message = "权限类型不能为空")
    private String permission;

    /**
     * 数据范围 (JSON)
     */
    private Object dataScope;
}
