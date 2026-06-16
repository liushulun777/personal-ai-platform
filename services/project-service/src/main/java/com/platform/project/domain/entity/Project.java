package com.platform.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 项目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_project")
public class Project extends BaseEntity {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 状态: 0-规划中, 1-进行中, 2-已完成, 3-已归档
     */
    private Integer status;

    /**
     * 优先级: 0-低, 1-中, 2-高, 3-紧急
     */
    private Integer priority;

    /**
     * 负责人ID
     */
    private Long ownerId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}
