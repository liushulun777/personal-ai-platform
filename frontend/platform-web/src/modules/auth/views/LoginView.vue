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
  <div class="min-h-screen flex items-center justify-center" style="background: var(--bg-base)">
    <div class="fixed inset-0 bg-gradient-to-br from-blue-500/[0.03] to-purple-500/[0.03]" />

    <NCard class="w-96 relative z-10 border border-[var(--border-color)]" content-style="padding: 32px;">
      <template #header>
        <div class="text-center">
          <h1 class="text-lg font-semibold tracking-wide" style="color: var(--text-primary)">AI Platform</h1>
          <p class="text-xs mt-1" style="color: var(--text-muted)">Personal Technical Platform</p>
        </div>
      </template>

      <NForm ref="formRef" :model="formData" :rules="rules">
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="formData.username" placeholder="请输入用户名" @keyup.enter="handleLogin" />
        </NFormItem>
        <NFormItem label="密码" path="password">
          <NInput v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </NFormItem>
        <NButton type="primary" block :loading="loading" @click="handleLogin" class="mt-2">
          登录
        </NButton>
      </NForm>

      <template #footer>
        <div class="text-center text-xs" style="color: var(--text-faint)">
          默认账号: admin / admin123
        </div>
      </template>
    </NCard>
  </div>
</template>
