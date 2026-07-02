<template>
  <n-layout has-sider style="height: 100vh">
    <!-- 侧边栏 -->
    <n-layout-sider
      bordered
      :collapsed="collapsed"
      :width="240"
      :collapsed-width="64"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <n-icon size="32" color="#18a058">
          <svg viewBox="0 0 24 24">
            <path fill="currentColor" d="M3 3h18v18H3V3zm2 2v14h14V5H5zm2 2h10v2H7V7zm0 4h10v2H7v-2zm0 4h7v2H7v-2z"/>
          </svg>
        </n-icon>
        <span v-if="!collapsed" class="title">报表平台</span>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>

    <!-- 内容区 -->
    <n-layout>
      <n-layout-header bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;">
        <n-breadcrumb>
          <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
            {{ item.title }}
          </n-breadcrumb-item>
        </n-breadcrumb>
        <n-space>
          <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
            <n-space style="cursor: pointer;">
              <n-avatar round size="small">{{ userInfo?.nickname?.charAt(0) || 'A' }}</n-avatar>
              <span>{{ userInfo?.nickname || 'Admin' }}</span>
            </n-space>
          </n-dropdown>
        </n-space>
      </n-layout-header>
      <n-layout-content content-style="padding: 24px;" :native-scrollbar="false">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuOption, DropdownOption } from 'naive-ui'
import { NIcon } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

const userInfo = computed(() => authStore.userInfo)

// 用户菜单选项
const userMenuOptions: DropdownOption[] = [
  { label: '个人中心', key: 'profile' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' }
]

// 用户菜单选择
const handleUserMenuSelect = async (key: string) => {
  if (key === 'logout') {
    await authStore.logout()
    router.push('/login')
  }
}

const activeKey = computed(() => {
  const path = route.path
  if (path.startsWith('/datasource')) return 'datasource'
  if (path.startsWith('/dataset')) return 'dataset'
  if (path.startsWith('/report')) return 'report/list'
  if (path.startsWith('/dashboard')) return 'dashboard/list'
  if (path.startsWith('/schedule')) return 'schedule'
  return ''
})

const breadcrumbs = computed(() => {
  const matched = route.matched
  return matched.map(item => ({
    path: item.path,
    title: (item.meta?.title as string) || ''
  })).filter(item => item.title)
})

// SVG路径常量
const SVG_PATHS = {
  database: 'M12 3C7.58 3 4 4.79 4 7v10c0 2.21 3.58 4 8 4s8-1.79 8-4V7c0-2.21-3.58-4-8-4zm0 2c3.87 0 6 1.5 6 2s-2.13 2-6 2-6-1.5-6-2 2.13-2 6-2zM6 17v-1.29c1.24.76 3.34 1.29 6 1.29s4.76-.53 6-1.29V17c0 .5-2.13 2-6 2s-6-1.5-6-2zm0-4v-1.29c1.24.76 3.34 1.29 6 1.29s4.76-.53 6-1.29V13c0 .5-2.13 2-6 2s-6-1.5-6-2zm0-4V7.71c1.24.76 3.34 1.29 6 1.29s4.76-.53 6-1.29V9c0 .5-2.13 2-6 2s-6-1.5-6-2z',
  table: 'M4 6h18V4H4c-1.1 0-2 .9-2 2v11H0v3h14v-3H4V6zm19 2h-6c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h6c.55 0 1-.45 1-1V9c0-.55-.45-1-1-1zm-1 9h-4v-7h4v7z',
  chart: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zM7 10h2v7H7zm4-3h2v10h-2zm4 6h2v4h-2z',
  dashboard: 'M21 3H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h7v2H8v2h8v-2h-2v-2h7c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 14H3V5h18v12z',
  schedule: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z'
}

// 创建图标组件
const createIcon = (path: string) => () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24' }, [h('path', { fill: 'currentColor', d: path })]) })

const DatabaseIcon = createIcon(SVG_PATHS.database)
const TableIcon = createIcon(SVG_PATHS.table)
const ChartIcon = createIcon(SVG_PATHS.chart)
const DashboardIcon = createIcon(SVG_PATHS.dashboard)
const ScheduleIcon = createIcon(SVG_PATHS.schedule)

const menuOptions: MenuOption[] = [
  { label: '数据源管理', key: 'datasource', icon: DatabaseIcon },
  { label: '数据集管理', key: 'dataset', icon: TableIcon },
  { label: '报表管理', key: 'report', icon: ChartIcon },
  { label: '大屏管理', key: 'dashboard', icon: DashboardIcon },
  { label: '调度管理', key: 'schedule', icon: ScheduleIcon }
]

const handleMenuClick = (key: string) => {
  router.push(`/${key}`)
}

// 获取用户信息
onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.userInfo) {
    try {
      await authStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgb(239, 239, 245);
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #18a058;
}
</style>
