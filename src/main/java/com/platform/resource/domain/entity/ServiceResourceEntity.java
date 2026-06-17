package com.platform.resource.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_resource")
public class ServiceResourceEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceName;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Double networkIO;
    private LocalDateTime recordTime;
}