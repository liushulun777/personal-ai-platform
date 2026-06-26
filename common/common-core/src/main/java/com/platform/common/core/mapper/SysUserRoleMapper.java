package com.platform.common.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.common.core.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据菜单ID列表查询权限标识
     */
    @Select("<script>" +
            "SELECT permission FROM sys_menu WHERE id IN " +
            "<foreach item='id' collection='menuIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND permission IS NOT NULL AND permission != ''" +
            "</script>")
    List<String> queryPermissionsByMenuIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 根据角色ID列表查询角色标识
     */
    @Select("<script>" +
            "SELECT role_key FROM sys_role WHERE id IN " +
            "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<String> queryRoleKeysByIds(@Param("roleIds") List<Long> roleIds);
}
