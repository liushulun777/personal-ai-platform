package com.platform.report.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.report.typehandler.JsonbTypeHandler;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_datasource", autoResultMap = true)
public class DataSource extends BaseEntity {

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源类型: MYSQL/POSTGRESQL/API/EXCEL/CSV
     */
    private String type;

    /**
     * 连接配置 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object config;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
