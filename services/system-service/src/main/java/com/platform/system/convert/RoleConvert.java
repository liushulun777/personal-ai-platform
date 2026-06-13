package com.platform.system.convert;

import com.platform.system.domain.dto.RoleCreateDTO;
import com.platform.system.domain.entity.SysRole;
import com.platform.system.domain.vo.RoleDetailVO;
import com.platform.system.domain.vo.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 角色转换器
 */
@Mapper(componentModel = "spring")
public interface RoleConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysRole createDTOToEntity(RoleCreateDTO dto);

    /**
     * 实体转VO
     */
    RoleVO entityToVO(SysRole entity);

    /**
     * 实体列表转VO列表
     */
    List<RoleVO> entityListToVOList(List<SysRole> entities);

    /**
     * 实体转详情VO
     */
    @Mapping(target = "menuIds", ignore = true)
    RoleDetailVO entityToDetailVO(SysRole entity);
}
