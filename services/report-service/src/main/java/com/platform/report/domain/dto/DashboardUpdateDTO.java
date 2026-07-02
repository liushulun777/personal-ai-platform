package com.platform.report.domain.dto;

import lombok.Data;

/**
 * 大屏更新DTO
 */
@Data
public class DashboardUpdateDTO {

    /**
     * 大屏名称
     */
    private String name;

    /**
     * 大屏配置 (JSON)
     */
    private Object config;

    /**
     * 备注
     */
    private String remark;
}
