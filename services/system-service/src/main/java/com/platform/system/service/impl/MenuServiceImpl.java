package com.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.system.convert.MenuConvert;
import com.platform.system.domain.dto.MenuCreateDTO;
import com.platform.system.domain.dto.MenuUpdateDTO;
import com.platform.system.domain.entity.SysMenu;
import com.platform.system.domain.entity.SysRoleMenu;
import com.platform.system.domain.vo.MenuVO;
import com.platform.system.mapper.SysMenuMapper;
import com.platform.system.mapper.SysRoleMenuMapper;
import com.platform.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final MenuConvert menuConvert;

    @Override
    public List<MenuVO> tree() {
        // 查询所有菜单
        List<SysMenu> menus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, CommonConstant.STATUS_ENABLED)
                        .eq(SysMenu::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(SysMenu::getSort)
        );

        // 转换为VO
        List<MenuVO> menuVOs = menuConvert.entityListToVOList(menus);

        // 构建树形结构
        return buildTree(menuVOs, 0L);
    }

    @Override
    public MenuVO getById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "菜单不存在");
        }
        return menuConvert.entityToVO(menu);
    }

    @Override
    public Long create(MenuCreateDTO createDTO) {
        SysMenu menu = menuConvert.createDTOToEntity(createDTO);
        menu.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        menu.setVisible(createDTO.getVisible() != null ? createDTO.getVisible() : CommonConstant.STATUS_ENABLED);
        menu.setSort(createDTO.getSort() != null ? createDTO.getSort() : 0);
        sysMenuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    public void update(Long id, MenuUpdateDTO updateDTO) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "菜单不存在");
        }

        menu.setParentId(updateDTO.getParentId());
        menu.setMenuName(updateDTO.getMenuName());
        menu.setPath(updateDTO.getPath());
        menu.setComponent(updateDTO.getComponent());
        menu.setIcon(updateDTO.getIcon());
        menu.setMenuType(updateDTO.getMenuType());
        menu.setPermission(updateDTO.getPermission());
        menu.setSort(updateDTO.getSort());
        menu.setVisible(updateDTO.getVisible());
        menu.setStatus(updateDTO.getStatus());
        sysMenuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        // 检查是否有子菜单
        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "存在子菜单，不能删除");
        }

        // 删除菜单
        sysMenuMapper.deleteById(id);

        // 删除角色菜单关联
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getMenuId, id)
        );
    }

    @Override
    public List<String> getPermissionsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询角色菜单关联
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        );

        if (roleMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取菜单ID列表
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        // 查询菜单
        List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);

        // 提取权限标识
        return menus.stream()
                .filter(menu -> StringUtils.hasText(menu.getPermission()))
                .map(SysMenu::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 构建树形结构
     */
    private List<MenuVO> buildTree(List<MenuVO> menus, Long parentId) {
        List<MenuVO> tree = new ArrayList<>();
        for (MenuVO menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                List<MenuVO> children = buildTree(menus, menu.getId());
                menu.setChildren(children.isEmpty() ? null : children);
                tree.add(menu);
            }
        }
        return tree;
    }
}
