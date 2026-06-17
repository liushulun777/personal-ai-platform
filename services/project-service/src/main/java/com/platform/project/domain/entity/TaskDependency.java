package com.platform.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务依赖关系实体
 */
@Data
@TableName("pm_task_dependency")
public class TaskDependency {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 依赖的任务ID
     */
    private Long dependencyTaskId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
