<script setup lang="ts">
import { computed } from 'vue'
import { darkTheme, NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, NLoadingBarProvider } from 'naive-ui'
import type { GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const theme = computed(() => themeStore.mode === 'dark' ? darkTheme : null)

const darkOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#3b82f6',
    primaryColorHover: '#60a5fa',
    primaryColorPressed: '#2563eb',
    bodyColor: '#0a0a0a',
    cardColor: '#141414',
    modalColor: '#141414',
    popoverColor: '#1a1a1a',
    tableColor: '#141414',
    inputColor: '#1a1a1a',
    actionColor: '#141414',
    borderColor: 'rgba(255,255,255,0.08)',
    dividerColor: 'rgba(255,255,255,0.06)',
    hoverColor: 'rgba(255,255,255,0.04)',
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    borderRadius: '8px',
    borderRadiusSmall: '6px'
  },
  Card: { borderColor: 'rgba(255,255,255,0.06)', color: '#141414' },
  Button: { borderRadiusMedium: '8px', borderRadiusSmall: '6px' },
  Input: { borderRadius: '8px', color: '#1a1a1a', borderColor: 'rgba(255,255,255,0.1)', borderHover: '#3b82f6', borderFocus: '#3b82f6' },
  Menu: {
    color: '#111',
    itemTextColor: '#a1a1aa',
    itemTextColorHover: '#e5e5e5',
    itemTextColorActive: '#e5e5e5',
    itemColorActive: 'rgba(59,130,246,0.1)',
    itemColorHover: 'rgba(255,255,255,0.04)',
    borderRadius: '6px'
  },
  DataTable: { borderColor: 'rgba(255,255,255,0.06)', thColor: '#111', tdColor: '#141414', tdColorHover: 'rgba(255,255,255,0.03)' },
  Tag: { borderRadius: '4px' }
}

const lightOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#2563eb',
    primaryColorHover: '#3b82f6',
    primaryColorPressed: '#1d4ed8',
    bodyColor: '#fafafa',
    cardColor: '#ffffff',
    modalColor: '#ffffff',
    popoverColor: '#ffffff',
    tableColor: '#ffffff',
    inputColor: '#ffffff',
    actionColor: '#ffffff',
    borderColor: '#e5e7eb',
    dividerColor: '#f0f0f0',
    hoverColor: 'rgba(0,0,0,0.02)',
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    borderRadius: '8px',
    borderRadiusSmall: '6px'
  },
  Card: { borderColor: '#e5e7eb', color: '#ffffff' },
  Button: { borderRadiusMedium: '8px', borderRadiusSmall: '6px' },
  Input: { borderRadius: '8px', color: '#ffffff', borderColor: '#d4d4d8', borderHover: '#2563eb', borderFocus: '#2563eb' },
  Menu: {
    color: '#ffffff',
    itemTextColor: '#52525b',
    itemTextColorHover: '#18181b',
    itemTextColorActive: '#18181b',
    itemColorActive: 'rgba(37,99,235,0.08)',
    itemColorHover: 'rgba(0,0,0,0.03)',
    borderRadius: '6px'
  },
  DataTable: { borderColor: '#e5e7eb', thColor: '#fafafa', tdColor: '#ffffff', tdColorHover: 'rgba(0,0,0,0.02)' },
  Tag: { borderRadius: '4px' }
}

const themeOverrides = computed(() => themeStore.mode === 'dark' ? darkOverrides : lightOverrides)
</script>

<template>
  <NConfigProvider :theme="theme" :theme-overrides="themeOverrides">
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <NLoadingBarProvider>
            <RouterView />
          </NLoadingBarProvider>
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
