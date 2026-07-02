package com.platform.report.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表详情VO
 */
@Data
public class ReportDetailVO {

    /**
     * 报表ID
     */
    private Long id;

    /**
     * 报表名称
     */
    private String name;

    /**
     * 报表编码
     */
    private String code;

    /**
     * 报表类型
     */
    private String type;

    /**
     * 报表分类
     */
    private String category;

    /**
     * 报表配置
     */
    private Object config;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
