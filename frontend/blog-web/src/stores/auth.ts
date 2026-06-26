import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import type { UserInfo, LoginParams, RegisterParams } from '@/api/auth'

export const useAuthStore = defineStore('blog-auth', () => {
  const token = ref<string>(localStorage.getItem('blog-token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(params: LoginParams) {
    const { data } = await loginApi(params)
    token.value = data.data.token
    userInfo.value = data.data.userInfo
    localStorage.setItem('blog-token', data.data.token)
  }

  async function register(params: RegisterParams) {
    await registerApi(params)
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const { data } = await getUserInfoApi()
      userInfo.value = data.data
    } catch {
      // token invalid
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('blog-token')
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // ignore
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('blog-token')
  }

  return { token, userInfo, isAuthenticated, login, register, fetchUserInfo, logout }
})
