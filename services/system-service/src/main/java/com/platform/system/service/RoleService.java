package com.platform.system.service;

import com.platform.common.core.result.PageResult;
import com.platform.system.domain.dto.RoleCreateDTO;
import com.platform.system.domain.dto.RoleQueryDTO;
import com.platform.system.domain.dto.RoleUpdateDTO;
import com.platform.system.domain.vo.RoleDetailVO;
import com.platform.system.domain.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 分页查询角色
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<RoleVO> page(RoleQueryDTO queryDTO);

    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> listAll();

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleDetailVO getById(Long id);

    /**
     * 创建角色
     *
     * @param createDTO 创建角色DTO
     * @return 角色ID
     */
    Long create(RoleCreateDTO createDTO);

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param updateDTO 更新角色DTO
     */
    void update(Long id, RoleUpdateDTO updateDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 分配菜单
     *
     * @param id 角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long id, List<Long> menuIds);
}
