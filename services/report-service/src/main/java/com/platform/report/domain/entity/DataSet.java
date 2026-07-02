package com.platform.report.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.report.typehandler.JsonbTypeHandler;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据集实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_dataset", autoResultMap = true)
public class DataSet extends BaseEntity {

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集类型: SQL/VISUAL/API
     */
    private String type;

    /**
     * 数据源ID
     */
    private Long sourceId;

    /**
     * 配置信息 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object config;

    /**
     * 字段列表 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object fields;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
