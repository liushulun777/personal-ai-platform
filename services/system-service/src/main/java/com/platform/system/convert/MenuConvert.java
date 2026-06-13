package com.platform.system.convert;

import com.platform.system.domain.dto.MenuCreateDTO;
import com.platform.system.domain.entity.SysMenu;
import com.platform.system.domain.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 菜单转换器
 */
@Mapper(componentModel = "spring")
public interface MenuConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysMenu createDTOToEntity(MenuCreateDTO dto);

    /**
     * 实体转VO
     */
    MenuVO entityToVO(SysMenu entity);

    /**
     * 实体列表转VO列表
     */
    List<MenuVO> entityListToVOList(List<SysMenu> entities);
}
