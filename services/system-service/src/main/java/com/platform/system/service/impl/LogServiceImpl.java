package com.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.system.domain.dto.LogQueryDTO;
import com.platform.system.domain.entity.SysLog;
import com.platform.system.domain.vo.LogVO;
import com.platform.system.mapper.SysLogMapper;
import com.platform.system.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final SysLogMapper sysLogMapper;

    @Override
    public PageResult<LogVO> page(LogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(queryDTO.getModule()), SysLog::getModule, queryDTO.getModule());
        wrapper.like(StringUtils.hasText(queryDTO.getOperation()), SysLog::getOperation, queryDTO.getOperation());
        wrapper.eq(queryDTO.getStatus() != null, SysLog::getStatus, queryDTO.getStatus());
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), SysLog::getUsername, queryDTO.getUsername());
        wrapper.ge(queryDTO.getStartTime() != null, SysLog::getCreateTime, queryDTO.getStartTime());
        wrapper.le(queryDTO.getEndTime() != null, SysLog::getCreateTime, queryDTO.getEndTime());
        wrapper.orderByDesc(SysLog::getCreateTime);

        Page<SysLog> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<SysLog> result = sysLogMapper.selectPage(page, wrapper);

        List<LogVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public LogVO getById(Long id) {
        SysLog sysLog = sysLogMapper.selectById(id);
        if (sysLog == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "日志不存在");
        }
        return toVO(sysLog);
    }

    @Override
    public void delete(Long id) {
        sysLogMapper.deleteById(id);
    }

    private LogVO toVO(SysLog entity) {
        LogVO vo = new LogVO();
        vo.setId(entity.getId());
        vo.setModule(entity.getModule());
        vo.setOperation(entity.getOperation());
        vo.setMethod(entity.getMethod());
        vo.setRequestUrl(entity.getRequestUrl());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setRequestParams(entity.getRequestParams());
        vo.setResponseData(entity.getResponseData());
        vo.setIp(entity.getIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setStatus(entity.getStatus());
        vo.setErrorMsg(entity.getErrorMsg());
        vo.setDuration(entity.getDuration());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
