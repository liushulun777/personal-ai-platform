package com.platform.common.security.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在 Controller 方法上，校验当前用户是否拥有指定权限
 *
 * 用法：
 *   @RequirePermission("system:user:list")
 *   @RequirePermission({"system:user:add", "system:user:edit"})
 *   @RequirePermission(value = {"system:user:add", "system:user:edit"}, mode = Mode.ALL)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限标识
     */
    String[] value();

    /**
     * 校验模式
     */
    Mode mode() default Mode.ANY;

    enum Mode {
        /** 拥有任意一个即可 */
        ANY,
        /** 必须拥有全部 */
        ALL
    }
}
