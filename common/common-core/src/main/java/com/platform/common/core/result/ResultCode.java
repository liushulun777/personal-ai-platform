package com.platform.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    FAIL(500, "系统异常"),

    // 认证相关 401x
    UNAUTHORIZED(401, "未登录或登录已过期"),
    TOKEN_INVALID(4011, "Token无效"),
    TOKEN_EXPIRED(4012, "Token已过期"),
    ACCOUNT_DISABLED(4013, "账号已被禁用"),
    ACCOUNT_LOCKED(4014, "账号已被锁定"),
    LOGIN_FAILED(4015, "用户名或密码错误"),

    // 权限相关 403x
    FORBIDDEN(403, "无权限访问"),

    // 参数相关 400x
    BAD_REQUEST(400, "请求参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),
    PARAM_INVALID(4002, "参数校验失败"),

    // 资源相关 404x
    NOT_FOUND(404, "资源不存在"),

    // 业务相关 5xxx
    BUSINESS_ERROR(5000, "业务异常"),
    DATA_DUPLICATE(5001, "数据重复"),
    DATA_NOT_FOUND(5002, "数据不存在"),
    OPTIMISTIC_LOCK_ERROR(5003, "数据已被修改，请刷新后重试");

    private final Integer code;
    private final String message;
}
