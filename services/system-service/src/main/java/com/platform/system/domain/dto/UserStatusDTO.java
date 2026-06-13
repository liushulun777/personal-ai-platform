package com.platform.system.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态DTO
 */
@Data
public class UserStatusDTO {

    /**
     * 状态: 1-启用, 0-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
