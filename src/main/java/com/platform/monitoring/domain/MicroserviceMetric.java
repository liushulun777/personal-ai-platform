package com.platform.monitoring.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微服务资源指标实体类
 */
@Data
@TableName("microservice_metric")
public class MicroserviceMetric {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微服务名称
     */
    private String serviceName;

    /**
     * 实例ID（例如：pod名称）
     */
    private String instanceId;

    /**
     * CPU使用率 (百分比，0-100)
     */
    private Double cpuUsage;

    /**
     * 内存使用率 (百分比，0-100)
     */
    private Double memoryUsage;

    /**
     * 内存使用量 (MB)
     */
    private Long memoryUsedMb;

    /**
     * 磁盘使用率 (百分比，可选)
     */
    private Double diskUsage;

    /**
     * 网络流量入 (KB/s，可选)
     */
    private Double networkInKbs;

    /**
     * 网络流量出 (KB/s，可选)
     */
    private Double networkOutKbs;

    /**
     * 数据采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 数据创建时间
     */
    private LocalDateTime createTime;
}