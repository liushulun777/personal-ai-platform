package com.platform.system.service;

import com.platform.common.core.result.PageResult;
import com.platform.system.domain.dto.UserCreateDTO;
import com.platform.system.domain.dto.UserQueryDTO;
import com.platform.system.domain.dto.UserStatusDTO;
import com.platform.system.domain.dto.UserUpdateDTO;
import com.platform.system.domain.vo.UserDetailVO;
import com.platform.system.domain.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<UserVO> page(UserQueryDTO queryDTO);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserDetailVO getById(Long id);

    /**
     * 创建用户
     *
     * @param createDTO 创建用户DTO
     * @return 用户ID
     */
    Long create(UserCreateDTO createDTO);

    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param updateDTO 更新用户DTO
     */
    void update(Long id, UserUpdateDTO updateDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void delete(Long id);

    /**
     * 修改用户状态
     *
     * @param id 用户ID
     * @param statusDTO 状态DTO
     */
    void updateStatus(Long id, UserStatusDTO statusDTO);

    /**
     * 分配角色
     *
     * @param id 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long id, List<Long> roleIds);
}
