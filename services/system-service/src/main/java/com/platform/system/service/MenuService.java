package com.platform.system.service;

import com.platform.system.domain.dto.MenuCreateDTO;
import com.platform.system.domain.dto.MenuUpdateDTO;
import com.platform.system.domain.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 获取菜单树形列表
     *
     * @return 菜单树
     */
    List<MenuVO> tree();

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    MenuVO getById(Long id);

    /**
     * 创建菜单
     *
     * @param createDTO 创建菜单DTO
     * @return 菜单ID
     */
    Long create(MenuCreateDTO createDTO);

    /**
     * 更新菜单
     *
     * @param id 菜单ID
     * @param updateDTO 更新菜单DTO
     */
    void update(Long id, MenuUpdateDTO updateDTO);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void delete(Long id);

    /**
     * 根据角色ID列表获取菜单权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限标识列表
     */
    List<String> getPermissionsByRoleIds(List<Long> roleIds);
}
