package com.platform.report.domain.dto;

import lombok.Data;

/**
 * 报表更新DTO
 */
@Data
public class ReportUpdateDTO {

    /**
     * 报表名称
     */
    private String name;

    /**
     * 报表分类
     */
    private String category;

    /**
     * 报表配置 (JSON)
     */
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
