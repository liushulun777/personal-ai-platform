package com.platform.report.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据集VO
 */
@Data
public class DataSetVO {

    /**
     * 数据集ID
     */
    private Long id;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集类型
     */
    private String type;

    /**
     * 数据源ID
     */
    private Long sourceId;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 配置信息
     */
    private Object config;

    /**
     * 字段列表
     */
    private Object fields;

    /**
     * 状态
     */
    private Integer status;

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
