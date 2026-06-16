package com.platform.project.convert;

import com.platform.project.domain.dto.TaskCreateDTO;
import com.platform.project.domain.dto.TaskUpdateDTO;
import com.platform.project.domain.entity.Task;
import com.platform.project.domain.vo.TaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 任务转换器
 */
@Mapper(componentModel = "spring")
public interface TaskConvert {

    TaskVO entityToVO(Task entity);

    List<TaskVO> entityListToVOList(List<Task> entities);

    Task createDTOToEntity(TaskCreateDTO dto);

    void updateEntityFromDTO(TaskUpdateDTO dto, @MappingTarget Task entity);
}
