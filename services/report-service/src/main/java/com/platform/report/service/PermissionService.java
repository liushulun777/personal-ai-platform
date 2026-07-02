package com.platform.report.service;

import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.PermissionCreateDTO;
import com.platform.report.domain.dto.PermissionQueryDTO;
import com.platform.report.domain.dto.PermissionUpdateDTO;
import com.platform.report.domain.vo.PermissionVO;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 分页查询权限
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<PermissionVO> page(PermissionQueryDTO queryDTO);

    /**
     * 创建权限
     *
     * @param createDTO 创建权限DTO
     * @return 权限ID
     */
    Long create(PermissionCreateDTO createDTO);

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param updateDTO 更新权限DTO
     */
    void update(Long id, PermissionUpdateDTO updateDTO);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void delete(Long id);

    /**
     * 检查权限
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param resourceType 资源类型
     * @param permission 权限类型
     * @return 是否有权限
     */
    boolean checkPermission(Long userId, Long resourceId, String resourceType, String permission);
}
