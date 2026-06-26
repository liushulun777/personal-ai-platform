<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, useMessage } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
    loading.value = true
    await authStore.login(formData.value.username, formData.value.password)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center relative overflow-hidden" style="background: var(--bg-base)">
    <!-- Animated gradient background -->
    <div class="absolute inset-0 pointer-events-none">
      <div
        class="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] rounded-full"
        style="background: radial-gradient(circle, rgba(129,140,248,0.12) 0%, transparent 70%); filter: blur(100px); animation: float 8s ease-in-out infinite"
      />
      <div
        class="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] rounded-full"
        style="background: radial-gradient(circle, rgba(192,132,252,0.1) 0%, transparent 70%); filter: blur(100px); animation: float 10s ease-in-out 2s infinite"
      />
      <div
        class="absolute top-[30%] right-[20%] w-[300px] h-[300px] rounded-full"
        style="background: radial-gradient(circle, rgba(244,114,182,0.08) 0%, transparent 70%); filter: blur(80px); animation: float 6s ease-in-out 4s infinite"
      />
    </div>

    <!-- Grid pattern overlay -->
    <div
      class="absolute inset-0 pointer-events-none opacity-[0.03]"
      style="background-image: linear-gradient(var(--text-faint) 1px, transparent 1px), linear-gradient(90deg, var(--text-faint) 1px, transparent 1px); background-size: 60px 60px"
    />

    <div class="relative z-10 animate-fade-in">
      <!-- Logo -->
      <div class="text-center mb-8">
        <h1 class="text-2xl font-extrabold tracking-wider gradient-text mb-2">AI Platform</h1>
        <p class="text-xs" style="color: var(--text-muted)">Personal Technical Platform</p>
      </div>

      <!-- Login card -->
      <div
        class="w-96 rounded-2xl p-8 glass"
        style="box-shadow: var(--shadow-lg)"
      >
        <NForm ref="formRef" :model="formData" :rules="rules">
          <NFormItem label="用户名" path="username">
            <NInput
              v-model:value="formData.username"
              placeholder="请输入用户名"
              class="!rounded-xl"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NFormItem label="密码" path="password">
            <NInput
              v-model:value="formData.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              class="!rounded-xl"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NButton
            type="primary"
            block
            :loading="loading"
            class="!mt-3 !rounded-xl !h-11 !text-sm !font-semibold"
            style="background: var(--accent-gradient); border: none; box-shadow: var(--glow)"
            @click="handleLogin"
          >
            登录
          </NButton>
        </NForm>

        <div class="mt-6 pt-4 text-center text-xs" style="border-top: 1px solid var(--border-color); color: var(--text-faint)">
          默认账号: admin / admin123
        </div>
      </div>
    </div>
  </div>
</template>
