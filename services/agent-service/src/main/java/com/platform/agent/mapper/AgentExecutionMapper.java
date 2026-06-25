package com.platform.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.agent.domain.entity.AgentExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Agent 执行记录 Mapper
 */
@Mapper
public interface AgentExecutionMapper extends BaseMapper<AgentExecution> {

    /**
     * 根据任务 ID 查询执行记录
     */
    @Select("SELECT * FROM agent_execution WHERE task_id = #{taskId} ORDER BY create_time DESC")
    List<AgentExecution> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据项目 ID 查询执行记录
     */
    @Select("SELECT * FROM agent_execution WHERE project_id = #{projectId} ORDER BY create_time DESC")
    List<AgentExecution> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询正在执行的任务
     */
    @Select("SELECT * FROM agent_execution WHERE status = 1 ORDER BY start_time ASC")
    List<AgentExecution> selectRunning();
}
