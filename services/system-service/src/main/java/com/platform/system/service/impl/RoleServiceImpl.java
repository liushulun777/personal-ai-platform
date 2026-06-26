package com.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.system.convert.RoleConvert;
import com.platform.system.domain.dto.RoleCreateDTO;
import com.platform.system.domain.dto.RoleQueryDTO;
import com.platform.system.domain.dto.RoleUpdateDTO;
import com.platform.common.core.entity.SysRoleMenu;
import com.platform.common.core.entity.SysUserRole;
import com.platform.common.core.mapper.SysRoleMenuMapper;
import com.platform.common.core.mapper.SysUserRoleMapper;
import com.platform.system.domain.entity.SysRole;
import com.platform.system.domain.vo.RoleDetailVO;
import com.platform.system.domain.vo.RoleVO;
import com.platform.system.mapper.SysRoleMapper;
import com.platform.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final RoleConvert roleConvert;

    @Override
    public PageResult<RoleVO> page(RoleQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRoleName()), SysRole::getRoleName, queryDTO.getRoleName());
        wrapper.eq(StringUtils.hasText(queryDTO.getRoleKey()), SysRole::getRoleKey, queryDTO.getRoleKey());
        wrapper.eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus());
        wrapper.orderByAsc(SysRole::getSort);

        // 分页查询
        Page<SysRole> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<SysRole> result = sysRoleMapper.selectPage(page, wrapper);

        // 转换结果
        List<RoleVO> records = roleConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<RoleVO> listAll() {
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, CommonConstant.STATUS_ENABLED)
                        .eq(SysRole::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(SysRole::getSort)
        );
        return roleConvert.entityListToVOList(roles);
    }

    @Override
    public RoleDetailVO getById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在");
        }

        RoleDetailVO detailVO = roleConvert.entityToDetailVO(role);

        // 查询角色菜单关联
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)
        );

        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        detailVO.setMenuIds(menuIds);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCreateDTO createDTO) {
        // 检查角色标识是否已存在
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleKey, createDTO.getRoleKey())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }

        // 创建角色
        SysRole role = roleConvert.createDTOToEntity(createDTO);
        role.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        role.setSort(createDTO.getSort() != null ? createDTO.getSort() : 0);
        sysRoleMapper.insert(role);

        // 分配菜单
        if (createDTO.getMenuIds() != null && !createDTO.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), createDTO.getMenuIds());
        }

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleUpdateDTO updateDTO) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在");
        }

        // 检查角色标识是否重复
        if (!role.getRoleKey().equals(updateDTO.getRoleKey())) {
            Long count = sysRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleKey, updateDTO.getRoleKey())
                            .ne(SysRole::getId, id)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_DUPLICATE, "角色标识已存在");
            }
        }

        role.setRoleName(updateDTO.getRoleName());
        role.setRoleKey(updateDTO.getRoleKey());
        role.setDescription(updateDTO.getDescription());
        role.setSort(updateDTO.getSort());
        role.setStatus(updateDTO.getStatus());
        sysRoleMapper.updateById(role);

        // 更新菜单
        if (updateDTO.getMenuIds() != null) {
            assignMenus(id, updateDTO.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查是否为超级管理员角色
        if (id.equals(CommonConstant.SUPER_ADMIN_ID)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能删除超级管理员角色");
        }

        // 检查是否有用户使用该角色
        Long userCount = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, id)
        );
        if (userCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该角色下存在用户，不能删除");
        }

        // 删除角色
        sysRoleMapper.deleteById(id);

        // 删除角色菜单关联
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long id, List<Long> menuIds) {
        // 删除原有角色菜单关联
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)
        );

        // 添加新的角色菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(id);
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
    }
}
