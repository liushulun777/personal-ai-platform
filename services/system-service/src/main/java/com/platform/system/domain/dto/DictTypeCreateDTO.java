package com.platform.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建字典类型DTO
 */
@Data
public class DictTypeCreateDTO {

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100个字符")
    private String dictName;

    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100个字符")
    private String dictType;

    /**
     * 描述
     */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}
