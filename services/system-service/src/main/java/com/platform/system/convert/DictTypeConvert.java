package com.platform.system.convert;

import com.platform.system.domain.dto.DictTypeCreateDTO;
import com.platform.system.domain.entity.SysDictType;
import com.platform.system.domain.vo.DictTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 字典类型转换器
 */
@Mapper(componentModel = "spring")
public interface DictTypeConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysDictType createDTOToEntity(DictTypeCreateDTO dto);

    /**
     * 实体转VO
     */
    DictTypeVO entityToVO(SysDictType entity);

    /**
     * 实体列表转VO列表
     */
    List<DictTypeVO> entityListToVOList(List<SysDictType> entities);
}
