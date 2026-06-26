package com.platform.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.auth.domain.dto.LoginDTO;
import com.platform.auth.domain.dto.RegisterDTO;
import com.platform.auth.domain.entity.SysMenu;
import com.platform.auth.domain.entity.SysRole;
import com.platform.auth.domain.entity.SysUser;
import com.platform.auth.domain.vo.LoginVO;
import com.platform.auth.domain.vo.UserInfoVO;
import com.platform.auth.mapper.SysMenuMapper;
import com.platform.auth.mapper.SysRoleMapper;
import com.platform.auth.mapper.SysUserMapper;
import com.platform.common.core.entity.SysRoleMenu;
import com.platform.common.core.entity.SysUserRole;
import com.platform.common.core.mapper.SysRoleMenuMapper;
import com.platform.common.core.mapper.SysUserRoleMapper;
import com.platform.auth.service.AuthService;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
                        .eq(SysUser::getDeleted, CommonConstant.NOT_DELETED)
        );

        // 用户不存在
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 用户已禁用
        if (CommonConstant.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 密码校验
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // Sa-Token登录
        StpUtil.login(user.getId());

        // 查询用户角色和权限
        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());

        // 构建用户信息
        UserInfoVO userInfoVO = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .build();

        // 返回登录信息
        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .tokenType("Bearer")
                .expiresIn(StpUtil.getTokenTimeout())
                .userInfo(userInfoVO)
                .build();
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, registerDTO.getUsername())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(CommonConstant.STATUS_ENABLED);

        sysUserMapper.insert(user);

        // 注册用户默认赋予作者角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L); // AUTHOR 角色
        sysUserRoleMapper.insert(userRole);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 查询用户角色和权限
        List<String> roles = getUserRoles(userId);
        List<String> permissions = getUserPermissions(userId);

        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    /**
     * 获取用户角色列表
     */
    private List<String> getUserRoles(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRole> roles = sysRoleMapper.selectByIds(roleIds);

        return roles.stream()
                .map(SysRole::getRoleKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户权限列表
     */
    private List<String> getUserPermissions(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

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

        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        List<SysMenu> menus = sysMenuMapper.selectByIds(menuIds);

        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}
