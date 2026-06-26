<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NButton, NDropdown } from 'naive-ui'
import { Search } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const themeStore = useThemeStore()
const authStore = useAuthStore()
const searchKeyword = ref('')

function handleSearch() {
  const q = searchKeyword.value.trim()
  if (q) {
    router.push({ path: '/search', query: { q } })
  }
}

function goToWrite() {
  // 跳转到 platform-web 的文章编辑器
  const platformUrl = import.meta.env.VITE_PLATFORM_URL || 'http://localhost:5174'
  window.open(`${platformUrl}/blog/articles/new`, '_blank')
}

const userDropdownOptions = [
  { label: '退出登录', key: 'logout' }
]

function handleUserDropdown(key: string) {
  if (key === 'logout') {
    authStore.logout()
    if (route.path.startsWith('/article')) {
      // stay on current page
    } else {
      router.push('/')
    }
  }
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    authStore.fetchUserInfo()
  }
})
</script>

<template>
  <div class="min-h-screen flex flex-col" style="background: var(--bg-base)">
    <!-- Header -->
    <header
      class="sticky top-0 z-50 h-16 flex items-center justify-between px-6 glass"
      style="border-bottom: 1px solid var(--border-color)"
    >
      <div class="flex items-center gap-8">
        <router-link to="/" class="text-base font-bold tracking-wider no-underline gradient-text">
          AI Platform
        </router-link>
        <nav class="flex items-center gap-1">
          <router-link
            to="/"
            class="text-sm no-underline px-3 py-1.5 rounded-lg transition-all duration-200"
            :style="{
              color: route.path === '/' ? 'var(--accent)' : 'var(--text-muted)',
              background: route.path === '/' ? 'var(--accent-soft)' : 'transparent'
            }"
          >
            首页
          </router-link>
        </nav>
      </div>
      <div class="flex items-center gap-3">
        <!-- Search -->
        <div
          class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs cursor-pointer transition-all duration-200 hover:border-[var(--border-hover)]"
          style="background: var(--hover-bg); border: 1px solid var(--border-color); min-width: 180px"
          @click="router.push('/search')"
        >
          <Search :size="13" style="color: var(--text-muted)" />
          <span style="color: var(--text-muted)">搜索文章...</span>
        </div>

        <NButton quaternary size="small" class="!rounded-lg" @click="themeStore.toggle">
          <template #icon>
            <span class="text-base transition-transform duration-300" :class="themeStore.mode === 'dark' ? 'rotate-0' : 'rotate-180'">
              {{ themeStore.mode === 'dark' ? '&#9789;' : '&#9728;' }}
            </span>
          </template>
        </NButton>

        <!-- User area -->
        <template v-if="authStore.isAuthenticated">
          <NButton
            size="small"
            class="!rounded-lg !px-4"
            style="background: var(--accent-gradient); border: none; color: #fff"
            @click="goToWrite"
          >
            写文章
          </NButton>
          <NDropdown :options="userDropdownOptions" @select="handleUserDropdown">
            <div class="flex items-center cursor-pointer gap-2 px-2 py-1 rounded-lg transition-all duration-200 hover:bg-[var(--hover-bg)]">
              <div
                class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white"
                style="background: var(--accent-gradient)"
              >
                {{ authStore.userInfo?.nickname?.[0] || authStore.userInfo?.username?.[0] || 'U' }}
              </div>
              <span class="text-sm" style="color: var(--text-secondary)">
                {{ authStore.userInfo?.nickname || authStore.userInfo?.username || '用户' }}
              </span>
            </div>
          </NDropdown>
        </template>
        <template v-else>
          <NButton
            size="small"
            class="!rounded-lg !px-4"
            style="background: var(--accent-gradient); border: none; color: #fff"
            @click="router.push('/login')"
          >
            登录
          </NButton>
        </template>
      </div>
    </header>

    <!-- Content -->
    <main class="flex-1">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer
      class="py-8 text-center"
      style="border-top: 1px solid var(--border-color)"
    >
      <div class="mb-2">
        <span class="text-sm font-semibold gradient-text">AI Platform</span>
      </div>
      <p class="text-xs" style="color: var(--text-faint)">
        &copy; {{ new Date().getFullYear() }} Personal AI Platform. Built with passion.
      </p>
    </footer>
  </div>
</template>
