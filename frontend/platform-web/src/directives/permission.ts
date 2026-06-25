import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * 权限指令
 * 用法：v-permission="'user:create'" 或 v-permission="['user:create', 'user:update']"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const authStore = useAuthStore()
    const permissions = authStore.userInfo?.permissions || []

    const requiredPermissions = Array.isArray(binding.value)
      ? binding.value
      : [binding.value]

    const hasPermission = requiredPermissions.some(p => permissions.includes(p))

    if (!hasPermission) {
      // 移除元素
      el.parentNode?.removeChild(el)
    }
  }
}

/**
 * 角色指令
 * 用法：v-role="'admin'" 或 v-role="['admin', 'editor']"
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const authStore = useAuthStore()
    const roles = authStore.userInfo?.roles || []

    const requiredRoles = Array.isArray(binding.value)
      ? binding.value
      : [binding.value]

    const hasRole = requiredRoles.some(r => roles.includes(r))

    if (!hasRole) {
      el.parentNode?.removeChild(el)
    }
  }
}
