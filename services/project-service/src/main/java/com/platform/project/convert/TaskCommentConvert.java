package com.platform.project.convert;

import com.platform.project.domain.dto.TaskCommentCreateDTO;
import com.platform.project.domain.entity.TaskComment;
import com.platform.project.domain.vo.TaskCommentVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 任务评论转换器
 */
@Mapper(componentModel = "spring")
public interface TaskCommentConvert {

    TaskCommentVO entityToVO(TaskComment entity);

    List<TaskCommentVO> entityListToVOList(List<TaskComment> entities);

    TaskComment createDTOToEntity(TaskCommentCreateDTO dto);
}
