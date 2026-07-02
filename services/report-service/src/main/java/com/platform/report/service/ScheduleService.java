package com.platform.report.service;

import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.ScheduleCreateDTO;
import com.platform.report.domain.dto.ScheduleQueryDTO;
import com.platform.report.domain.dto.ScheduleUpdateDTO;
import com.platform.report.domain.vo.ScheduleDetailVO;
import com.platform.report.domain.vo.ScheduleVO;

/**
 * 调度服务接口
 */
public interface ScheduleService {

    /**
     * 分页查询调度任务
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ScheduleVO> page(ScheduleQueryDTO queryDTO);

    /**
     * 获取调度任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    ScheduleDetailVO getById(Long id);

    /**
     * 创建调度任务
     *
     * @param createDTO 创建任务DTO
     * @return 任务ID
     */
    Long create(ScheduleCreateDTO createDTO);

    /**
     * 更新调度任务
     *
     * @param id 任务ID
     * @param updateDTO 更新任务DTO
     */
    void update(Long id, ScheduleUpdateDTO updateDTO);

    /**
     * 删除调度任务
     *
     * @param id 任务ID
     */
    void delete(Long id);

    /**
     * 启动调度任务
     *
     * @param id 任务ID
     */
    void start(Long id);

    /**
     * 停止调度任务
     *
     * @param id 任务ID
     */
    void stop(Long id);

    /**
     * 暂停调度任务
     *
     * @param id 任务ID
     */
    void pause(Long id);
}
