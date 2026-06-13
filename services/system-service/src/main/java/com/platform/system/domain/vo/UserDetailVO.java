package com.platform.system.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailVO extends UserVO {

    /**
     * 角色列表
     */
    private List<RoleVO> roles;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
