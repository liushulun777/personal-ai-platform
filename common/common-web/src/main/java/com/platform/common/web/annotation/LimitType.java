package com.platform.common.web.annotation;

/**
 * 限流类型枚举
 */
public enum LimitType {

    /**
     * 默认（按方法限流）
     */
    DEFAULT,

    /**
     * 按 IP 限流
     */
    IP,

    /**
     * 按用户限流
     */
    USER
}
