package com.platform.common.web.aspect;

import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.common.web.annotation.LimitType;
import com.platform.common.web.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 接口限流切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    /**
     * Lua 脚本：原子性限流
     */
    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local current = tonumber(redis.call('get', key) or '0')\n" +
            "if current >= limit then\n" +
            "    return 0\n" +
            "else\n" +
            "    current = redis.call('incr', key)\n" +
            "    if current == 1 then\n" +
            "        redis.call('expire', key, window)\n" +
            "    end\n" +
            "    return 1\n" +
            "end";

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint joinPoint, RateLimit rateLimit) {
        // 获取限流 key
        String key = getRateLimitKey(joinPoint, rateLimit);

        // 执行限流检查
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(rateLimit.limit()),
                String.valueOf(rateLimit.window())
        );

        if (result != null && result == 0) {
            log.warn("接口限流: key={}, limit={}, window={}", key, rateLimit.limit(), rateLimit.window());
            throw new BusinessException(ResultCode.BUSINESS_ERROR, rateLimit.message());
        }
    }

    /**
     * 获取限流 key
     */
    private String getRateLimitKey(JoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder("rate_limit:");

        // 自定义 key
        if (rateLimit.key() != null && !rateLimit.key().isEmpty()) {
            key.append(rateLimit.key());
        } else {
            // 方法全路径
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            key.append(method.getDeclaringClass().getName())
               .append(".")
               .append(method.getName());
        }

        // 根据限流类型追加后缀
        switch (rateLimit.type()) {
            case IP:
                key.append(":").append(getClientIp());
                break;
            case USER:
                key.append(":").append(getUserId());
                break;
            default:
                break;
        }

        return key.toString();
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取用户 ID（从 Sa-Token 获取）
     */
    private String getUserId() {
        try {
            return cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
