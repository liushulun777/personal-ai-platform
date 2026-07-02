package com.platform.report.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据集创建DTO
 */
@Data
public class DataSetCreateDTO {

    /**
     * 数据集名称
     */
    @NotBlank(message = "数据集名称不能为空")
    private String name;

    /**
     * 数据集类型: SQL/VISUAL/API
     */
    @NotBlank(message = "数据集类型不能为空")
    private String type;

    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    private Long sourceId;

    /**
     * 配置信息 (JSON)
     */
    @NotNull(message = "配置信息不能为空")
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
