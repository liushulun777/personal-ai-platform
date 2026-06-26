package com.platform.common.web.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.core.annotation.OperationLog;
import com.platform.common.core.handler.OperationLogHandler;
import com.platform.common.core.handler.OperationLogHandler.OperationLogInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 通用操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final List<OperationLogHandler> logHandlers;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        OperationLogInfo logInfo = new OperationLogInfo();
        logInfo.setModule(operationLog.module());
        logInfo.setOperation(operationLog.operation());
        logInfo.setType(operationLog.type().name());
        logInfo.setMethod(joinPoint.getSignature().toShortString());

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logInfo.setRequestUrl(request.getRequestURI());
            logInfo.setRequestMethod(request.getMethod());
            logInfo.setIp(getClientIp(request));
            logInfo.setUserAgent(request.getHeader("User-Agent"));
        }

        // 获取请求参数
        if (operationLog.saveParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                if (args.length > 0) {
                    String params = objectMapper.writeValueAsString(args);
                    logInfo.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
                }
            } catch (Exception e) {
                log.warn("序列化请求参数失败", e);
            }
        }

        Object result;
        try {
            result = joinPoint.proceed();
            logInfo.setStatus(1);

            // 获取响应数据
            if (operationLog.saveResult()) {
                try {
                    String responseData = objectMapper.writeValueAsString(result);
                    logInfo.setResponseData(responseData.length() > 2000 ? responseData.substring(0, 2000) : responseData);
                } catch (Exception e) {
                    log.warn("序列化响应数据失败", e);
                }
            }
        } catch (Throwable e) {
            logInfo.setStatus(0);
            logInfo.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 500
                    ? e.getMessage().substring(0, 500) : e.getMessage());
            throw e;
        } finally {
            logInfo.setDuration(System.currentTimeMillis() - startTime);
            // 调用所有日志处理器
            for (OperationLogHandler handler : logHandlers) {
                try {
                    handler.saveLog(logInfo);
                } catch (Exception e) {
                    log.error("保存操作日志失败", e);
                }
            }
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
