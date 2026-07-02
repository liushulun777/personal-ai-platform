package com.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.ScheduleCreateDTO;
import com.platform.report.domain.dto.ScheduleQueryDTO;
import com.platform.report.domain.dto.ScheduleUpdateDTO;
import com.platform.report.domain.entity.ReportDefinition;
import com.platform.report.domain.entity.Schedule;
import com.platform.report.domain.vo.ScheduleDetailVO;
import com.platform.report.domain.vo.ScheduleVO;
import com.platform.report.mapper.ReportDefinitionMapper;
import com.platform.report.mapper.ScheduleMapper;
import com.platform.report.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 调度服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final ReportDefinitionMapper reportDefinitionMapper;

    @Override
    public PageResult<ScheduleVO> page(ScheduleQueryDTO queryDTO) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getName()), Schedule::getName, queryDTO.getName())
                .eq(queryDTO.getReportId() != null, Schedule::getReportId, queryDTO.getReportId())
                .eq(queryDTO.getStatus() != null, Schedule::getStatus, queryDTO.getStatus())
                .orderByDesc(Schedule::getCreateTime);

        Page<Schedule> page = scheduleMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        List<ScheduleVO> voList = BeanUtil.copyToList(page.getRecords(), ScheduleVO.class);
        // 填充报表名称
        voList.forEach(vo -> {
            ReportDefinition report = reportDefinitionMapper.selectById(vo.getReportId());
            if (report != null) {
                vo.setReportName(report.getName());
            }
        });

        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ScheduleDetailVO getById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("调度任务不存在");
        }
        ScheduleDetailVO vo = BeanUtil.copyProperties(schedule, ScheduleDetailVO.class);
        // 填充报表名称
        ReportDefinition report = reportDefinitionMapper.selectById(schedule.getReportId());
        if (report != null) {
            vo.setReportName(report.getName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ScheduleCreateDTO createDTO) {
        // 验证报表是否存在
        ReportDefinition report = reportDefinitionMapper.selectById(createDTO.getReportId());
        if (report == null) {
            throw new BusinessException("报表不存在");
        }

        Schedule schedule = BeanUtil.copyProperties(createDTO, Schedule.class);
        schedule.setStatus(0); // 停止
        scheduleMapper.insert(schedule);
        return schedule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ScheduleUpdateDTO updateDTO) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("调度任务不存在");
        }
        BeanUtil.copyProperties(updateDTO, schedule, "id");
        scheduleMapper.updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("调度任务不存在");
        }
        scheduleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("调度任务不存在");
        }
        schedule.setStatus(1); // 运行
        // TODO: 计算下次执行时间
        scheduleMapper.updateById(schedule);
        log.info("启动调度任务: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stop(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("调度任务不存在");
        }
        schedule.setStatus(0); // 停止
        schedule.setNextExecuteTime(null);
        scheduleMapper.updateById(schedule);
        log.info("停止调度任务: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("调度任务不存在");
        }
        schedule.setStatus(2); // 暂停
        scheduleMapper.updateById(schedule);
        log.info("暂停调度任务: id={}", id);
    }
}
