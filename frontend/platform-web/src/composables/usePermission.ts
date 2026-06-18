import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * 权限检查组合式函数
 */
export function usePermission() {
  const authStore = useAuthStore()

  /**
   * 获取用户权限列表
   */
  const userPermissions = computed(() => authStore.userInfo?.permissions || [])

  /**
   * 获取用户角色列表
   */
  const userRoles = computed(() => authStore.userInfo?.roles || [])

  /**
   * 检查是否有指定权限
   */
  function hasPermission(permission: string | string[]): boolean {
    const permissions = userPermissions.value
    const requiredPermissions = Array.isArray(permission) ? permission : [permission]
    return requiredPermissions.some(p => permissions.includes(p))
  }

  /**
   * 检查是否有指定角色
   */
  function hasRole(role: string | string[]): boolean {
    const roles = userRoles.value
    const requiredRoles = Array.isArray(role) ? role : [role]
    return requiredRoles.some(r => roles.includes(r))
  }

  /**
   * 检查是否有所有权限
   */
  function hasAllPermissions(permissions: string[]): boolean {
    return permissions.every(p => userPermissions.value.includes(p))
  }

  /**
   * 检查是否有任意权限
   */
  function hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(p => userPermissions.value.includes(p))
  }

  /**
   * 是否是管理员
   */
  const isAdmin = computed(() => hasRole('admin'))

  return {
    hasPermission,
    hasRole,
    hasAllPermissions,
    hasAnyPermission,
    userPermissions,
    userRoles,
    isAdmin
  }
}
