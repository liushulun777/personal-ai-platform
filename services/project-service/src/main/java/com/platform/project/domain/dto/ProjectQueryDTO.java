package com.platform.project.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectQueryDTO extends PageQuery {

    /**
     * 项目名称（模糊查询）
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 负责人ID
     */
    private Long ownerId;
}
