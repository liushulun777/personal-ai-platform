<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NResult, NButton } from 'naive-ui'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen flex items-center justify-center" style="background: var(--bg-base)">
    <NResult status="404" title="404" description="页面不存在">
      <template #footer>
        <NButton type="primary" size="small" @click="router.push('/')">返回首页</NButton>
      </template>
    </NResult>
  </div>
</template>
