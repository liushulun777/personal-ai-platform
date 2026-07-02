package com.platform.report.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据源创建DTO
 */
@Data
public class DataSourceCreateDTO {

    /**
     * 数据源名称
     */
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    /**
     * 数据源类型: MYSQL/POSTGRESQL/API/EXCEL/CSV
     */
    @NotBlank(message = "数据源类型不能为空")
    private String type;

    /**
     * 连接配置 (JSON)
     */
    @NotNull(message = "连接配置不能为空")
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
