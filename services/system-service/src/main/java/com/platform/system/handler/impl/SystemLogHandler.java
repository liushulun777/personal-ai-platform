package com.platform.system.handler.impl;

import com.platform.common.core.handler.OperationLogHandler;
import com.platform.system.domain.entity.SysLog;
import com.platform.system.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 系统服务日志处理器实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemLogHandler implements OperationLogHandler {

    private final SysLogMapper sysLogMapper;

    @Override
    public void saveLog(OperationLogInfo logInfo) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setModule(logInfo.getModule());
            sysLog.setOperation(logInfo.getOperation());
            sysLog.setMethod(logInfo.getMethod());
            sysLog.setRequestUrl(logInfo.getRequestUrl());
            sysLog.setRequestMethod(logInfo.getRequestMethod());
            sysLog.setIp(logInfo.getIp());
            sysLog.setUserAgent(logInfo.getUserAgent());
            sysLog.setRequestParams(logInfo.getRequestParams());
            sysLog.setResponseData(logInfo.getResponseData());
            sysLog.setErrorMsg(logInfo.getErrorMsg());
            sysLog.setStatus(logInfo.getStatus());
            sysLog.setDuration(logInfo.getDuration());
            sysLog.setUserId(logInfo.getUserId());
            sysLog.setCreateTime(LocalDateTime.now());

            sysLogMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
