package com.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.PermissionCreateDTO;
import com.platform.report.domain.dto.PermissionQueryDTO;
import com.platform.report.domain.dto.PermissionUpdateDTO;
import com.platform.report.domain.entity.ReportPermission;
import com.platform.report.domain.vo.PermissionVO;
import com.platform.report.mapper.ReportPermissionMapper;
import com.platform.report.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final ReportPermissionMapper permissionMapper;

    @Override
    public PageResult<PermissionVO> page(PermissionQueryDTO queryDTO) {
        LambdaQueryWrapper<ReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getUserId() != null, ReportPermission::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getRoleId() != null, ReportPermission::getRoleId, queryDTO.getRoleId())
                .eq(queryDTO.getResourceId() != null, ReportPermission::getResourceId, queryDTO.getResourceId())
                .eq(queryDTO.getResourceType() != null, ReportPermission::getResourceType, queryDTO.getResourceType())
                .orderByDesc(ReportPermission::getCreateTime);

        Page<ReportPermission> page = permissionMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        return PageResult.of(BeanUtil.copyToList(page.getRecords(), PermissionVO.class), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PermissionCreateDTO createDTO) {
        // 验证用户或角色必须有一个
        if (createDTO.getUserId() == null && createDTO.getRoleId() == null) {
            throw new BusinessException("用户ID和角色ID不能同时为空");
        }

        ReportPermission permission = BeanUtil.copyProperties(createDTO, ReportPermission.class);
        permissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PermissionUpdateDTO updateDTO) {
        ReportPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        BeanUtil.copyProperties(updateDTO, permission, "id");
        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ReportPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        permissionMapper.deleteById(id);
    }

    @Override
    public boolean checkPermission(Long userId, Long resourceId, String resourceType, String permission) {
        LambdaQueryWrapper<ReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPermission::getResourceId, resourceId)
                .eq(ReportPermission::getResourceType, resourceType)
                .eq(ReportPermission::getPermission, permission)
                .and(w -> w.eq(ReportPermission::getUserId, userId)
                        .or()
                        .inSql(ReportPermission::getRoleId,
                                "SELECT role_id FROM sys_user_role WHERE user_id = " + userId));

        return permissionMapper.selectCount(wrapper) > 0;
    }
}
