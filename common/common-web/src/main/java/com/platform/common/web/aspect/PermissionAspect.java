package com.platform.common.web.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.common.security.annotation.RequireRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 权限校验切面
 * 处理 @RequirePermission 和 @RequireRole 注解
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 权限校验
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String[] permissions = requirePermission.value();
        if (permissions.length == 0) return;

        Object loginId = StpUtil.getLoginId();
        List<String> userPermissions = StpUtil.getPermissionList();
        log.info("权限校验: 用户={}, 需要={}, 拥有={}", loginId, Arrays.toString(permissions), userPermissions);

        if (requirePermission.mode() == RequirePermission.Mode.ALL) {
            for (String perm : permissions) {
                if (!StpUtil.hasPermission(perm)) {
                    log.warn("权限校验失败: 用户={}, 需要={}, 拥有={}", loginId, perm, userPermissions);
                    throw new BusinessException(ResultCode.FORBIDDEN, "无操作权限: " + perm);
                }
            }
        } else {
            boolean hasAny = Arrays.stream(permissions).anyMatch(StpUtil::hasPermission);
            if (!hasAny) {
                log.warn("权限校验失败: 用户={}, 需要{}中任意一个, 拥有={}", loginId, Arrays.toString(permissions), userPermissions);
                throw new BusinessException(ResultCode.FORBIDDEN, "无操作权限");
            }
        }
    }

    /**
     * 角色校验
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        String[] roles = requireRole.value();
        if (roles.length == 0) return;

        Object loginId = StpUtil.getLoginId();
        List<String> userRoles = StpUtil.getRoleList();
        log.info("角色校验: 用户={}, 需要={}, 拥有={}", loginId, Arrays.toString(roles), userRoles);

        boolean hasAny = Arrays.stream(roles).anyMatch(StpUtil::hasRole);
        if (!hasAny) {
            log.warn("角色校验失败: 用户={}, 需要{}, 拥有={}", loginId, Arrays.toString(roles), userRoles);
            throw new BusinessException(ResultCode.FORBIDDEN, "无操作权限");
        }
    }
}
