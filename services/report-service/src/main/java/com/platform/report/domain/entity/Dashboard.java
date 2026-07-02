package com.platform.report.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.report.typehandler.JsonbTypeHandler;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大屏实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_dashboard", autoResultMap = true)
public class Dashboard extends BaseEntity {

    /**
     * 大屏名称
     */
    private String name;

    /**
     * 大屏配置 (JSON)
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object config;

    /**
     * 状态: 0-草稿 1-已发布 2-已归档
     */
    private Integer status;

    /**
     * 分享链接
     */
    private String shareUrl;

    /**
     * 备注
     */
    private String remark;
}
