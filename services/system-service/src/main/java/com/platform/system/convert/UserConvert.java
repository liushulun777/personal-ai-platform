package com.platform.system.convert;

import com.platform.system.domain.dto.UserCreateDTO;
import com.platform.system.domain.entity.SysRole;
import com.platform.system.domain.entity.SysUser;
import com.platform.system.domain.vo.RoleVO;
import com.platform.system.domain.vo.UserDetailVO;
import com.platform.system.domain.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 用户转换器
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysUser createDTOToEntity(UserCreateDTO dto);

    /**
     * 实体转列表VO
     */
    UserVO entityToVO(SysUser entity);

    /**
     * 实体列表转VO列表
     */
    List<UserVO> entityListToVOList(List<SysUser> entities);

    /**
     * 实体转详情VO
     */
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    UserDetailVO entityToDetailVO(SysUser entity);

    /**
     * 角色实体转VO
     */
    RoleVO roleEntityToVO(SysRole entity);

    /**
     * 角色实体列表转VO列表
     */
    List<RoleVO> roleEntityListToVOList(List<SysRole> entities);
}
