package com.platform.report.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.report.typehandler.JsonbTypeHandler;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_permission", autoResultMap = true)
public class ReportPermission extends BaseEntity {

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
     * 资源类型: DATASOURCE/DATASET/REPORT/DASHBOARD
     */
    private String resourceType;

    /**
     * 权限类型: VIEW/EDIT/DELETE/EXPORT/PRINT
     */
    private String permission;

    /**
     * 数据范围 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object dataScope;
}
