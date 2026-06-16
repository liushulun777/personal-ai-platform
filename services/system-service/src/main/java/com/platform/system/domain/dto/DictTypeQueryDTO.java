package com.platform.system.domain.dto;

import com.platform.common.core.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeQueryDTO extends PageQuery {

    /**
     * 字典名称（模糊查询）
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
