package com.platform.report.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.report.typehandler.JsonbTypeHandler;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_definition", autoResultMap = true)
public class ReportDefinition extends BaseEntity {

    /**
     * 报表名称
     */
    private String name;

    /**
     * 报表编码
     */
    private String code;

    /**
     * 报表类型: NORMAL/COMPLEX/DASHBOARD
     */
    private String type;

    /**
     * 报表分类
     */
    private String category;

    /**
     * 报表配置 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object config;

    /**
     * 状态: 0-草稿 1-已发布 2-已归档
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 备注
     */
    private String remark;
}
