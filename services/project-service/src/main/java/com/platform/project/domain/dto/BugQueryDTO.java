package com.platform.project.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Bug查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BugQueryDTO extends PageQuery {

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * Bug标题（模糊查询）
     */
    private String title;

    /**
     * 严重程度
     */
    private Integer severity;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long assigneeId;
}
