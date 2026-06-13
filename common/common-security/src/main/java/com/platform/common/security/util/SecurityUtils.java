package com.platform.common.security.util;

import cn.dev33.satoken.stp.StpUtil;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;

/**
 * 安全工具类
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前登录用户ID（不抛异常，未登录返回null）
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }

    /**
     * 检查是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        return StpUtil.hasPermission(permission);
    }
}
