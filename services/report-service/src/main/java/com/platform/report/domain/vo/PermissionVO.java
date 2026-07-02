package com.platform.report.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限VO
 */
@Data
public class PermissionVO {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 权限类型
     */
    private String permission;

    /**
     * 数据范围
     */
    private Object dataScope;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
