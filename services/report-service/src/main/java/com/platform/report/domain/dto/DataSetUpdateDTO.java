package com.platform.report.domain.dto;

import lombok.Data;

/**
 * 数据集更新DTO
 */
@Data
public class DataSetUpdateDTO {

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 配置信息 (JSON)
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
