package com.platform.report.domain.dto;

import lombok.Data;

/**
 * 权限更新DTO
 */
@Data
public class PermissionUpdateDTO {

    /**
     * 权限类型: VIEW/EDIT/DELETE/EXPORT/PRINT
     */
    private String permission;

    /**
     * 数据范围 (JSON)
     */
    private Object dataScope;
}
