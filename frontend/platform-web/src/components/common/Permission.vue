<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

interface Props {
  /** 需要的权限 */
  permission?: string | string[]
  /** 需要的角色 */
  role?: string | string[]
  /** 是否需要所有权限（默认任意一个即可） */
  requireAll?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  requireAll: false
})

const authStore = useAuthStore()

const hasAccess = computed(() => {
  const permissions = authStore.userInfo?.permissions || []
  const roles = authStore.userInfo?.roles || []

  // 检查角色
  if (props.role) {
    const requiredRoles = Array.isArray(props.role) ? props.role : [props.role]
    const hasRole = requiredRoles.some(r => roles.includes(r))
    if (!hasRole) return false
  }

  // 检查权限
  if (props.permission) {
    const requiredPermissions = Array.isArray(props.permission) ? props.permission : [props.permission]

    if (props.requireAll) {
      // 需要所有权限
      return requiredPermissions.every(p => permissions.includes(p))
    } else {
      // 任意一个权限即可
      return requiredPermissions.some(p => permissions.includes(p))
    }
  }

  return true
})
</script>

<template>
  <slot v-if="hasAccess" />
</template>
