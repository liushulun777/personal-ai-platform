package com.platform.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.project.domain.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务评论Mapper
 */
@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {
}
