package com.platform.common.security.util;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * 数据权限工具类
 * 根据用户角色的数据范围过滤查询
 */
public class DataScopeUtils {

    /**
     * 数据范围常量
     */
    public static final int SCOPE_ALL = 1;   // 全部数据
    public static final int SCOPE_OWN = 2;   // 仅本人数据

    /**
     * 获取当前用户ID（未登录返回 null）
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            if (StpUtil.isLogin()) {
                return Long.parseLong(StpUtil.getLoginId().toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 判断当前用户是否有全部数据权限
     * 管理员角色拥有全部数据权限，其他角色仅本人数据
     */
    public static boolean hasAllDataScope() {
        try {
            if (StpUtil.hasRole("SUPER_ADMIN") || StpUtil.hasRole("admin")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据数据范围条件过滤查询
     *
     * @param wrapper      查询条件
     * @param authorField  作者字段引用
     * @param currentUserId 当前用户ID
     * @param <T>          实体类型
     */
    public static <T> void applyDataScope(LambdaQueryWrapper<T> wrapper,
                                           SFunction<T, ?> authorField,
                                           Long currentUserId) {
        if (currentUserId == null) {
            // 未登录，不限制（公开数据）
            return;
        }
        if (!hasAllDataScope()) {
            // 仅本人数据
            wrapper.eq(authorField, currentUserId);
        }
        // 全部数据，不做限制
    }
}
