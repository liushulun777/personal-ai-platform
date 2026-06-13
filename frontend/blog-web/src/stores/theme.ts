import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'dark' | 'light'

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>((localStorage.getItem('blog-theme') as ThemeMode) || 'dark')

  function toggle() {
    mode.value = mode.value === 'dark' ? 'light' : 'dark'
  }

  watch(mode, (val) => {
    document.documentElement.setAttribute('data-theme', val)
    localStorage.setItem('blog-theme', val)
  }, { immediate: true })

  return { mode, toggle }
})
