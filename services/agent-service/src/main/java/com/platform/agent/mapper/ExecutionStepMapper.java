package com.platform.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.agent.domain.entity.ExecutionStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 执行步骤 Mapper
 */
@Mapper
public interface ExecutionStepMapper extends BaseMapper<ExecutionStep> {

    /**
     * 根据执行记录 ID 查询步骤
     */
    @Select("SELECT * FROM agent_execution_step WHERE execution_id = #{executionId} ORDER BY step_order ASC")
    List<ExecutionStep> selectByExecutionId(@Param("executionId") Long executionId);
}
