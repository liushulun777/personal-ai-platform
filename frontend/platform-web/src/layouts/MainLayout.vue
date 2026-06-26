<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
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

// 检查是否有指定权限
function hasPerm(perm: string): boolean {
  const perms = authStore.userInfo?.permissions || []
  return perms.includes(perm)
}

// 构建菜单（根据权限过滤）
const menuOptions = computed<MenuOption[]>(() => {
  const items: MenuOption[] = []

  // 仪表盘
  if (hasPerm('system:dashboard:view')) {
    items.push({
      label: '仪表盘',
      key: 'Dashboard',
      icon: renderIcon('D')
    })
  }

  // 系统管理
  const systemChildren: MenuOption[] = []
  if (hasPerm('system:user:list')) systemChildren.push({ label: '用户管理', key: 'UserList' })
  if (hasPerm('system:role:list')) systemChildren.push({ label: '角色管理', key: 'RoleList' })
  if (hasPerm('system:menu:list')) systemChildren.push({ label: '菜单管理', key: 'MenuList' })
  if (hasPerm('system:dict:list')) systemChildren.push({ label: '字典管理', key: 'DictList' })
  if (hasPerm('system:log:list')) systemChildren.push({ label: '操作日志', key: 'LogList' })
  if (systemChildren.length) {
    items.push({ label: '系统管理', key: 'System', icon: renderIcon('S'), children: systemChildren })
  }

  // 博客管理
  const blogChildren: MenuOption[] = []
  if (hasPerm('blog:article:list')) blogChildren.push({ label: '文章管理', key: 'ArticleList' })
  if (hasPerm('blog:category:list')) blogChildren.push({ label: '分类管理', key: 'CategoryList' })
  if (hasPerm('blog:tag:list')) blogChildren.push({ label: '标签管理', key: 'TagList' })
  if (blogChildren.length) {
    items.push({ label: '博客管理', key: 'Blog', icon: renderIcon('B'), children: blogChildren })
  }

  // 知识库
  const kbChildren: MenuOption[] = []
  if (hasPerm('knowledge:document:list')) kbChildren.push({ label: '文档管理', key: 'DocumentList' })
  if (hasPerm('knowledge:chat:use')) kbChildren.push({ label: '知识问答', key: 'KnowledgeChat' })
  if (kbChildren.length) {
    items.push({ label: '知识库', key: 'Knowledge', icon: renderIcon('K'), children: kbChildren })
  }

  // AI 助手
  const aiChildren: MenuOption[] = []
  if (hasPerm('ai:chat:use')) aiChildren.push({ label: 'AI 对话', key: 'AiChat' })
  if (hasPerm('ai:prompt:list')) aiChildren.push({ label: 'Prompt 模板', key: 'PromptList' })
  if (hasPerm('ai:prompt:market')) aiChildren.push({ label: 'Prompt 市场', key: 'PromptMarket' })
  if (aiChildren.length) {
    items.push({ label: 'AI 助手', key: 'AI', icon: renderIcon('A'), children: aiChildren })
  }

  // 项目管理
  const projectChildren: MenuOption[] = []
  if (hasPerm('project:project:list')) projectChildren.push({ label: '项目管理', key: 'ProjectList' })
  if (hasPerm('project:task:list')) projectChildren.push({ label: '任务管理', key: 'TaskList' })
  if (hasPerm('project:task:board')) projectChildren.push({ label: '任务看板', key: 'TaskBoard' })
  if (hasPerm('project:bug:list')) projectChildren.push({ label: 'Bug管理', key: 'BugList' })
  if (hasPerm('project:bug:stats')) projectChildren.push({ label: 'Bug统计', key: 'BugStats' })
  if (hasPerm('project:requirement:list')) projectChildren.push({ label: '需求讨论', key: 'RequirementDiscussion' })
  if (projectChildren.length) {
    items.push({ label: '项目管理', key: 'Project', icon: renderIcon('P'), children: projectChildren })
  }

  // MCP 平台
  const mcpChildren: MenuOption[] = []
  if (hasPerm('mcp:server:list')) mcpChildren.push({ label: 'MCP 服务', key: 'McpServerList' })
  if (hasPerm('mcp:tool:list')) mcpChildren.push({ label: 'MCP 工具', key: 'McpToolList' })
  if (mcpChildren.length) {
    items.push({ label: 'MCP 平台', key: 'MCP', icon: renderIcon('M'), children: mcpChildren })
  }

  // Agent
  const agentChildren: MenuOption[] = []
  if (hasPerm('agent:execution:list')) agentChildren.push({ label: '执行记录', key: 'AgentExecution' })
  if (agentChildren.length) {
    items.push({ label: 'Agent', key: 'Agent', icon: renderIcon('R'), children: agentChildren })
  }

  return items
})

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

onMounted(async () => {
  // 确保用户信息已加载（含权限列表）
  if (authStore.isAuthenticated && !authStore.userInfo?.permissions?.length) {
    await authStore.getUserInfo()
  }
})
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
      <!-- Brand -->
      <div class="h-16 flex items-center px-5 border-b border-[var(--border-color)] relative overflow-hidden">
        <div class="absolute top-0 left-0 right-0 h-0.5" style="background: var(--accent-gradient)" />
        <span v-if="!collapsed" class="text-base font-bold tracking-wider gradient-text">AI Platform</span>
        <span v-else class="text-sm font-bold gradient-text">AP</span>
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
      <!-- Header -->
      <div
        class="flex-shrink-0 h-16 flex items-center justify-between px-6 glass"
        style="border-bottom: 1px solid var(--border-color)"
      >
        <div class="text-sm font-medium" style="color: var(--text-muted)">
          {{ route.meta.title }}
        </div>
        <div class="flex items-center gap-3">
          <NButton quaternary size="small" class="!rounded-lg" @click="themeStore.toggle">
            <template #icon>
              <span class="text-base transition-transform duration-300" :class="themeStore.mode === 'dark' ? 'rotate-0' : 'rotate-180'">
                {{ themeStore.mode === 'dark' ? '&#9789;' : '&#9728;' }}
              </span>
            </template>
          </NButton>
          <NDropdown :options="userDropdownOptions" @select="handleUserDropdown">
            <div class="flex items-center cursor-pointer gap-2.5 px-2 py-1 rounded-lg transition-all duration-200 hover:bg-[var(--hover-bg)]">
              <NAvatar round :size="30" style="background: var(--accent-gradient)">
                <span class="text-xs font-bold text-white">{{ authStore.userInfo?.nickname?.[0] || 'U' }}</span>
              </NAvatar>
              <span class="text-sm font-medium" style="color: var(--text-secondary)">{{ authStore.userInfo?.nickname || '用户' }}</span>
            </div>
          </NDropdown>
        </div>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto p-6">
        <RouterView />
        <NBackTop :right="40" :bottom="40" />
      </div>
    </div>
  </NLayout>
</template>
