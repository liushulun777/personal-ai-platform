<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NLayout, NLayoutSider, NMenu, NAvatar, NDropdown, NButton } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const collapsed = ref(false)

function renderIcon(text: string) {
  return () => h('span', { class: 'text-sm font-medium w-5 text-center inline-block' }, text)
}

const menuOptions: MenuOption[] = [
  {
    label: '仪表盘',
    key: 'Dashboard',
    icon: renderIcon('D')
  },
  {
    label: '系统管理',
    key: 'System',
    icon: renderIcon('S'),
    children: [
      { label: '用户管理', key: 'UserList' },
      { label: '角色管理', key: 'RoleList' },
      { label: '菜单管理', key: 'MenuList' }
    ]
  },
  {
    label: '博客管理',
    key: 'Blog',
    icon: renderIcon('B'),
    children: [
      { label: '文章管理', key: 'ArticleList' },
      { label: '分类管理', key: 'CategoryList' },
      { label: '标签管理', key: 'TagList' }
    ]
  }
]

function handleMenuUpdate(key: string) {
  router.push({ name: key })
}

const userDropdownOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '退出登录', key: 'logout' }
]

function handleUserDropdown(key: string) {
  if (key === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<template>
  <NLayout has-sider class="h-screen overflow-hidden">
    <!-- Sidebar -->
    <NLayoutSider
      collapse-mode="width"
      :collapsed-width="56"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      :native-scrollbar="false"
      class="border-r border-[var(--border-color)]"
      :content-style="`background: var(--bg-sidebar);`"
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="h-14 flex items-center px-5 border-b border-[var(--border-color)]">
        <span v-if="!collapsed" class="text-sm font-semibold tracking-wide" style="color: var(--text-primary)">AI Platform</span>
        <span v-else class="text-sm font-semibold" style="color: var(--text-primary)">AP</span>
      </div>
      <NMenu
        :collapsed="collapsed"
        :collapsed-width="56"
        :collapsed-icon-size="18"
        :options="menuOptions"
        :value="route.name as string"
        @update:value="handleMenuUpdate"
      />
    </NLayoutSider>

    <!-- Right side -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0" style="background: var(--bg-base)">
      <!-- Header — fixed -->
      <div class="flex-shrink-0 h-14 flex items-center justify-between px-6 border-b border-[var(--border-color)]" style="background: var(--bg-base)">
        <div class="text-sm" style="color: var(--text-muted)">
          {{ route.meta.title }}
        </div>
        <div class="flex items-center gap-3">
          <NButton quaternary size="small" @click="themeStore.toggle">
            <template #icon>
              <span class="text-base">{{ themeStore.mode === 'dark' ? '&#9789;' : '&#9728;' }}</span>
            </template>
          </NButton>
          <NDropdown :options="userDropdownOptions" @select="handleUserDropdown">
            <div class="flex items-center cursor-pointer gap-2 hover:opacity-80 transition-opacity">
              <NAvatar round :size="28" color="#3b82f6">
                <span class="text-xs font-medium text-white">{{ authStore.userInfo?.nickname?.[0] || 'U' }}</span>
              </NAvatar>
              <span class="text-sm" style="color: var(--text-secondary)">{{ authStore.userInfo?.nickname || '用户' }}</span>
            </div>
          </NDropdown>
        </div>
      </div>

      <!-- Content — scrollable -->
      <div class="flex-1 overflow-y-auto p-6">
        <RouterView />
        <NBackTop :right="40" :bottom="40" />
      </div>
    </div>
  </NLayout>
</template>
