package com.platform.common.web.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流 key（默认使用方法全路径）
     */
    String key() default "";

    /**
     * 时间窗口内最大请求数（默认 100）
     */
    int limit() default 100;

    /**
     * 时间窗口（秒，默认 60 秒）
     */
    int window() default 60;

    /**
     * 限流类型
     */
    LimitType type() default LimitType.DEFAULT;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
