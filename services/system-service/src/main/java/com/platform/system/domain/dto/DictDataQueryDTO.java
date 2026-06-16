package com.platform.system.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataQueryDTO extends PageQuery {

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签（模糊查询）
     */
    private String dictLabel;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
