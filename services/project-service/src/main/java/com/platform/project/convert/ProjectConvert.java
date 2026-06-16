package com.platform.project.convert;

import com.platform.project.domain.dto.ProjectCreateDTO;
import com.platform.project.domain.dto.ProjectUpdateDTO;
import com.platform.project.domain.entity.Project;
import com.platform.project.domain.vo.ProjectVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 项目转换器
 */
@Mapper(componentModel = "spring")
public interface ProjectConvert {

    ProjectVO entityToVO(Project entity);

    List<ProjectVO> entityListToVOList(List<Project> entities);

    Project createDTOToEntity(ProjectCreateDTO dto);

    void updateEntityFromDTO(ProjectUpdateDTO dto, @MappingTarget Project entity);
}
