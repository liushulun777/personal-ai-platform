package com.platform.report.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报表创建DTO
 */
@Data
public class ReportCreateDTO {

    /**
     * 报表名称
     */
    @NotBlank(message = "报表名称不能为空")
    private String name;

    /**
     * 报表编码
     */
    @NotBlank(message = "报表编码不能为空")
    private String code;

    /**
     * 报表类型: NORMAL/COMPLEX/DASHBOARD
     */
    @NotBlank(message = "报表类型不能为空")
    private String type;

    /**
     * 报表分类
     */
    private String category;

    /**
     * 报表配置 (JSON)
     */
    @NotNull(message = "报表配置不能为空")
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
