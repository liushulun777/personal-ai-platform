package com.platform.report.domain.dto;

import lombok.Data;

/**
 * 数据源更新DTO
 */
@Data
public class DataSourceUpdateDTO {

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 连接配置 (JSON)
     */
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
