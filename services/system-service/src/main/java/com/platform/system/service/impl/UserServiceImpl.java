package com.platform.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.domain.entity.BaseEntity;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.system.convert.UserConvert;
import com.platform.system.domain.dto.UserCreateDTO;
import com.platform.system.domain.dto.UserQueryDTO;
import com.platform.system.domain.dto.UserStatusDTO;
import com.platform.system.domain.dto.UserUpdateDTO;
import com.platform.system.domain.entity.SysRole;
import com.platform.system.domain.entity.SysUser;
import com.platform.system.domain.entity.SysUserRole;
import com.platform.system.domain.vo.RoleVO;
import com.platform.system.domain.vo.UserDetailVO;
import com.platform.system.domain.vo.UserVO;
import com.platform.system.mapper.SysRoleMapper;
import com.platform.system.mapper.SysUserMapper;
import com.platform.system.mapper.SysUserRoleMapper;
import com.platform.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserConvert userConvert;

    @Override
    public PageResult<UserVO> page(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername());
        wrapper.like(StringUtils.hasText(queryDTO.getNickname()), SysUser::getNickname, queryDTO.getNickname());
        wrapper.eq(StringUtils.hasText(queryDTO.getPhone()), SysUser::getPhone, queryDTO.getPhone());
        wrapper.eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(SysUser::getCreateTime);

        // 分页查询
        Page<SysUser> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        // 转换结果
        List<UserVO> records = userConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public UserDetailVO getById(Long id) {
        // 查询用户
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 转换为详情VO
        UserDetailVO detailVO = userConvert.entityToDetailVO(user);

        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id)
        );

        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            detailVO.setRoleIds(roleIds);

            // 查询角色详情
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            List<RoleVO> roleVOs = userConvert.roleEntityListToVOList(roles);
            detailVO.setRoles(roleVOs);
        }

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCreateDTO createDTO) {
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, createDTO.getUsername())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "用户名已存在");
        }

        // 创建用户
        SysUser user = userConvert.createDTOToEntity(createDTO);
        user.setPassword(BCrypt.hashpw(createDTO.getPassword()));
        user.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        sysUserMapper.insert(user);

        // 分配角色
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), createDTO.getRoleIds());
        }

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UserUpdateDTO updateDTO) {
        // 查询用户
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 更新用户信息
        user.setNickname(updateDTO.getNickname());
        user.setEmail(updateDTO.getEmail());
        user.setPhone(updateDTO.getPhone());
        user.setAvatar(updateDTO.getAvatar());
        sysUserMapper.updateById(user);

        // 更新角色
        if (updateDTO.getRoleIds() != null) {
            assignRoles(id, updateDTO.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查是否为超级管理员
        if (id.equals(CommonConstant.SUPER_ADMIN_ID)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能删除超级管理员");
        }

        // 逻辑删除用户
        sysUserMapper.deleteById(id);

        // 删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id)
        );
    }

    @Override
    public void updateStatus(Long id, UserStatusDTO statusDTO) {
        // 检查是否为超级管理员
        if (id.equals(CommonConstant.SUPER_ADMIN_ID)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能修改超级管理员状态");
        }

        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        user.setStatus(statusDTO.getStatus());
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long id, List<Long> roleIds) {
        // 删除原有角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id)
        );

        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
    }
}
