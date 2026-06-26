package com.platform.common.security.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 标注在 Controller 方法上，校验当前用户是否拥有指定角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 角色标识
     */
    String[] value();
}
