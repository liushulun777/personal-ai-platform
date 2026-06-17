package com.platform.common.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 接口访问日志拦截器 - 记录请求耗时
 */
@Slf4j
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        // 超过 1 秒的请求记录警告
        if (duration > 1000) {
            log.warn("慢请求: {} {} - {}ms - status={}", method, uri, duration, status);
        } else if (log.isDebugEnabled()) {
            log.debug("请求: {} {} - {}ms - status={}", method, uri, duration, status);
        }
    }
}
