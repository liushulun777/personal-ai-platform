package com.platform.project.convert;

import com.platform.project.domain.dto.TaskExecutionCreateDTO;
import com.platform.project.domain.entity.TaskExecution;
import com.platform.project.domain.vo.TaskExecutionVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 任务执行记录转换器
 */
@Mapper(componentModel = "spring")
public interface TaskExecutionConvert {

    TaskExecutionVO entityToVO(TaskExecution entity);

    List<TaskExecutionVO> entityListToVOList(List<TaskExecution> entities);

    TaskExecution createDTOToEntity(TaskExecutionCreateDTO dto);
}
