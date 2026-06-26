<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NInput, NButton, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const tab = ref<'login' | 'register'>('login')
const loading = ref(false)

// Login form
const loginForm = ref({ username: '', password: '' })

// Register form
const regForm = ref({ username: '', password: '', confirmPassword: '', nickname: '', email: '' })

async function handleLogin() {
  if (!loginForm.value.username.trim() || !loginForm.value.password) {
    message.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(loginForm.value)
    message.success('登录成功')
    router.push('/')
  } catch (e: any) {
    message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!regForm.value.username.trim() || !regForm.value.password) {
    message.warning('请输入用户名和密码')
    return
  }
  if (regForm.value.password.length < 6) {
    message.warning('密码长度不能少于6位')
    return
  }
  if (regForm.value.password !== regForm.value.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }
  if (regForm.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(regForm.value.email)) {
    message.warning('邮箱格式不正确')
    return
  }
  loading.value = true
  try {
    await authStore.register({
      username: regForm.value.username,
      password: regForm.value.password,
      nickname: regForm.value.nickname || undefined,
      email: regForm.value.email || undefined
    })
    message.success('注册成功，请登录')
    tab.value = 'login'
    loginForm.value.username = regForm.value.username
    loginForm.value.password = ''
  } catch (e: any) {
    message.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}

function handleKeyup(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    tab.value === 'login' ? handleLogin() : handleRegister()
  }
}
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center px-6 py-16 animate-fade-in">
    <!-- Decorative background -->
    <div class="fixed inset-0 pointer-events-none">
      <div
        class="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] rounded-full"
        style="background: radial-gradient(circle, rgba(129,140,248,0.08) 0%, transparent 70%); filter: blur(80px)"
      />
      <div
        class="absolute bottom-[-20%] right-[-10%] w-[400px] h-[400px] rounded-full"
        style="background: radial-gradient(circle, rgba(192,132,252,0.06) 0%, transparent 70%); filter: blur(80px)"
      />
    </div>

    <div class="relative z-10 w-full max-w-sm">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-2xl font-extrabold gradient-text mb-2">AI Platform</h1>
        <p class="text-xs" style="color: var(--text-muted)">加入我们，探索技术世界</p>
      </div>

      <!-- Card -->
      <div
        class="rounded-2xl p-7 glass"
        style="box-shadow: var(--shadow-lg)"
      >
        <!-- Tabs -->
        <div class="flex mb-6 rounded-xl p-1" style="background: var(--hover-bg)">
          <button
            class="flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200"
            :style="{
              background: tab === 'login' ? 'var(--accent-gradient)' : 'transparent',
              color: tab === 'login' ? '#fff' : 'var(--text-muted)',
              boxShadow: tab === 'login' ? 'var(--glow)' : 'none'
            }"
            @click="tab = 'login'"
          >
            登录
          </button>
          <button
            class="flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200"
            :style="{
              background: tab === 'register' ? 'var(--accent-gradient)' : 'transparent',
              color: tab === 'register' ? '#fff' : 'var(--text-muted)',
              boxShadow: tab === 'register' ? 'var(--glow)' : 'none'
            }"
            @click="tab = 'register'"
          >
            注册
          </button>
        </div>

        <!-- Login Form -->
        <div v-if="tab === 'login'" class="space-y-4">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">用户名</label>
            <NInput
              v-model:value="loginForm.username"
              placeholder="请输入用户名"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">密码</label>
            <NInput
              v-model:value="loginForm.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <NButton
            type="primary"
            block
            :loading="loading"
            class="!rounded-xl !h-10 !text-sm !font-semibold !mt-2"
            style="background: var(--accent-gradient); border: none; box-shadow: var(--glow)"
            @click="handleLogin"
          >
            登录
          </NButton>
          <div class="text-center text-xs" style="color: var(--text-faint)">
            默认账号: admin / admin123
          </div>
        </div>

        <!-- Register Form -->
        <div v-else class="space-y-3.5">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">用户名 <span style="color: #ef4444">*</span></label>
            <NInput
              v-model:value="regForm.username"
              placeholder="4-50个字符"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">密码 <span style="color: #ef4444">*</span></label>
            <NInput
              v-model:value="regForm.password"
              type="password"
              show-password-on="click"
              placeholder="至少6位"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">确认密码 <span style="color: #ef4444">*</span></label>
            <NInput
              v-model:value="regForm.confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="再次输入密码"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">昵称</label>
            <NInput
              v-model:value="regForm.nickname"
              placeholder="可选"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary)">邮箱</label>
            <NInput
              v-model:value="regForm.email"
              placeholder="可选，用于找回密码"
              class="!rounded-xl"
              @keyup="handleKeyup"
            />
          </div>
          <NButton
            type="primary"
            block
            :loading="loading"
            class="!rounded-xl !h-10 !text-sm !font-semibold !mt-2"
            style="background: var(--accent-gradient); border: none; box-shadow: var(--glow)"
            @click="handleRegister"
          >
            注册
          </NButton>
        </div>
      </div>
    </div>
  </div>
</template>
