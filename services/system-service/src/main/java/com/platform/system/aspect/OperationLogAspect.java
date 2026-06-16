package com.platform.system.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.security.util.SecurityUtils;
import com.platform.system.annotation.OperationLog;
import com.platform.system.domain.entity.SysLog;
import com.platform.system.mapper.SysLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysLogMapper sysLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        SysLog sysLog = new SysLog();
        sysLog.setModule(operationLog.module());
        sysLog.setOperation(operationLog.operation());
        sysLog.setMethod(joinPoint.getSignature().toShortString());
        sysLog.setCreateTime(LocalDateTime.now());

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            sysLog.setRequestUrl(request.getRequestURI());
            sysLog.setRequestMethod(request.getMethod());
            sysLog.setIp(getClientIp(request));
            sysLog.setUserAgent(request.getHeader("User-Agent"));
        }

        // 获取请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                String params = objectMapper.writeValueAsString(args);
                sysLog.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
        }

        // 获取当前用户
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            sysLog.setUserId(userId);
        }

        Object result;
        try {
            result = joinPoint.proceed();
            sysLog.setStatus(1);

            // 获取响应数据
            try {
                String responseData = objectMapper.writeValueAsString(result);
                sysLog.setResponseData(responseData.length() > 2000 ? responseData.substring(0, 2000) : responseData);
            } catch (Exception e) {
                log.warn("序列化响应数据失败", e);
            }
        } catch (Throwable e) {
            sysLog.setStatus(0);
            sysLog.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 500
                    ? e.getMessage().substring(0, 500) : e.getMessage());
            throw e;
        } finally {
            sysLog.setDuration(System.currentTimeMillis() - startTime);
            // 异步保存日志
            try {
                sysLogMapper.insert(sysLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
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
