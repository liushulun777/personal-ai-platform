package com.platform.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.auth.domain.entity.SysMenu;
import com.platform.auth.domain.entity.SysRole;
import com.platform.auth.domain.entity.SysRoleMenu;
import com.platform.auth.domain.entity.SysUserRole;
import com.platform.auth.mapper.SysMenuMapper;
import com.platform.auth.mapper.SysRoleMapper;
import com.platform.auth.mapper.SysRoleMenuMapper;
import com.platform.auth.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限认证实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());

        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询角色菜单关联
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        );

        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询菜单权限
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);

        return menus.stream()
                .filter(menu -> StringUtils.hasText(menu.getPermission()))
                .map(SysMenu::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());

        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询角色详情
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);

        return roles.stream()
                .map(SysRole::getRoleKey)
                .collect(Collectors.toList());
    }
}
