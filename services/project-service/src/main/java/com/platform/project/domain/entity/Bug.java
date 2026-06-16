package com.platform.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Bug实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_bug")
public class Bug extends BaseEntity {

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * Bug标题
     */
    private String title;

    /**
     * Bug描述
     */
    private String description;

    /**
     * 严重程度: 0-轻微, 1-一般, 2-严重, 3-致命
     */
    private Integer severity;

    /**
     * 状态: 0-待确认, 1-已确认, 2-修复中, 3-已修复, 4-已关闭
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long assigneeId;

    /**
     * 报告人ID
     */
    private Long reporterId;

    /**
     * 截止日期
     */
    private LocalDate dueDate;
}
