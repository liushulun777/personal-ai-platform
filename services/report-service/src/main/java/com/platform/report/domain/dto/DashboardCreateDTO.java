package com.platform.report.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 大屏创建DTO
 */
@Data
public class DashboardCreateDTO {

    /**
     * 大屏名称
     */
    @NotBlank(message = "大屏名称不能为空")
    private String name;

    /**
     * 大屏配置 (JSON)
     */
    @NotNull(message = "大屏配置不能为空")
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
