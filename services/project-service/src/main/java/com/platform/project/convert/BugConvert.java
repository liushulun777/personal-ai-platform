package com.platform.project.convert;

import com.platform.project.domain.dto.BugCreateDTO;
import com.platform.project.domain.dto.BugUpdateDTO;
import com.platform.project.domain.entity.Bug;
import com.platform.project.domain.vo.BugVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Bug转换器
 */
@Mapper(componentModel = "spring")
public interface BugConvert {

    BugVO entityToVO(Bug entity);

    List<BugVO> entityListToVOList(List<Bug> entities);

    Bug createDTOToEntity(BugCreateDTO dto);

    void updateEntityFromDTO(BugUpdateDTO dto, @MappingTarget Bug entity);
}
