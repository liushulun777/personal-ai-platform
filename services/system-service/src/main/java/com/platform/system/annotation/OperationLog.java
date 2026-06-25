package com.platform.system.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 模块
     */
    String module() default "";

    /**
     * 操作
     */
    String operation() default "";

    /**
     * 操作类型：CREATE-新增, UPDATE-修改, DELETE-删除, QUERY-查询, EXPORT-导出, OTHER-其他
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 是否保存请求参数
     */
    boolean saveParams() default true;

    /**
     * 是否保存响应数据
     */
    boolean saveResult() default false;
}
