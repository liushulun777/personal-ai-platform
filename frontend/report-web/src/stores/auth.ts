import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import type { UserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const data = await loginApi({ username, password })
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
  }

  async function getUserInfo() {
    const data = await getUserInfoApi()
    userInfo.value = data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // 忽略退出登录接口错误
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    login,
    getUserInfo,
    logout
  }
})
