<script setup lang="ts">
import { computed } from 'vue'
import { NConfigProvider, darkTheme, NMessageProvider, NBackTop } from 'naive-ui'
import type { GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const theme = computed(() => (themeStore.mode === 'dark' ? darkTheme : null))

const darkOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#3b82f6',
    primaryColorHover: '#60a5fa',
    primaryColorPressed: '#2563eb',
    borderRadius: '8px',
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif'
  }
}

const lightOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#2563eb',
    primaryColorHover: '#3b82f6',
    primaryColorPressed: '#1d4ed8',
    borderRadius: '8px',
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif'
  }
}

const overrides = computed(() => (themeStore.mode === 'dark' ? darkOverrides : lightOverrides))
</script>

<template>
  <NConfigProvider :theme="theme" :theme-overrides="overrides">
    <NMessageProvider>
      <RouterView />
      <NBackTop :right="40" :bottom="40" />
    </NMessageProvider>
  </NConfigProvider>
</template>
