<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { NButton } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const route = useRoute()
const themeStore = useThemeStore()
</script>

<template>
  <div class="min-h-screen flex flex-col" style="background: var(--bg-base)">
    <!-- Header -->
    <header
      class="sticky top-0 z-50 h-14 flex items-center justify-between px-6 backdrop-blur-md"
      style="background: var(--bg-header); border-bottom: 1px solid var(--border-color)"
    >
      <div class="flex items-center gap-6">
        <router-link to="/" class="text-sm font-semibold tracking-wide no-underline" style="color: var(--text-primary)">
          AI Platform
        </router-link>
        <nav class="flex items-center gap-4">
          <router-link
            to="/"
            class="text-sm no-underline transition-colors"
            :style="{ color: route.path === '/' ? 'var(--accent)' : 'var(--text-muted)' }"
          >
            首页
          </router-link>
        </nav>
      </div>
      <div class="flex items-center gap-3">
        <NButton quaternary size="small" @click="themeStore.toggle">
          <template #icon>
            <span class="text-base">{{ themeStore.mode === 'dark' ? '&#9789;' : '&#9728;' }}</span>
          </template>
        </NButton>
      </div>
    </header>

    <!-- Content -->
    <main class="flex-1">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer
      class="py-6 text-center text-xs"
      style="color: var(--text-faint); border-top: 1px solid var(--border-color)"
    >
      &copy; {{ new Date().getFullYear() }} Personal AI Platform. All rights reserved.
    </footer>
  </div>
</template>
