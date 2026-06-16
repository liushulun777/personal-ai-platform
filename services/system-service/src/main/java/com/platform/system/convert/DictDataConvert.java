package com.platform.system.convert;

import com.platform.system.domain.dto.DictDataCreateDTO;
import com.platform.system.domain.entity.SysDictData;
import com.platform.system.domain.vo.DictDataVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 字典数据转换器
 */
@Mapper(componentModel = "spring")
public interface DictDataConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysDictData createDTOToEntity(DictDataCreateDTO dto);

    /**
     * 实体转VO
     */
    DictDataVO entityToVO(SysDictData entity);

    /**
     * 实体列表转VO列表
     */
    List<DictDataVO> entityListToVOList(List<SysDictData> entities);
}
