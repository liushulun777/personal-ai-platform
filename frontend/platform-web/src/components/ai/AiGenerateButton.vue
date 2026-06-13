<template>
  <n-popover trigger="click" placement="bottom" :width="320" @update:show="onShow">
    <template #trigger>
      <n-button
        :loading="loading"
        :disabled="disabled"
        size="small"
        type="primary"
        ghost
      >
        <template #icon>
          <n-icon><sparkles-icon /></n-icon>
        </template>
        {{ label }}
      </n-button>
    </template>

    <div v-if="candidates.length > 0" class="ai-candidates">
      <n-radio-group v-model:value="selected" vertical>
        <n-radio
          v-for="(item, index) in candidates"
          :key="index"
          :value="item"
          style="margin-bottom: 8px"
        >
          {{ item }}
        </n-radio>
      </n-radio-group>

      <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px">
        <n-button size="tiny" @click="handleGenerate" :loading="loading">重新生成</n-button>
        <n-button size="tiny" type="primary" @click="handleUse">使用</n-button>
      </div>
    </div>

    <div v-else-if="loading" style="text-align: center; padding: 20px">
      <n-spin size="small" />
      <p style="margin-top: 8px; color: #999; font-size: 12px">AI 生成中...</p>
    </div>

    <div v-else style="text-align: center; padding: 12px; color: #999; font-size: 12px">
      点击按钮开始生成
    </div>
  </n-popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { SparklesOutline as SparklesIcon } from '@vicons/ionicons5'

interface Props {
  label: string
  disabled?: boolean
  generateFn: () => Promise<string[]>
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'use', value: string): void
}>()

const loading = ref(false)
const candidates = ref<string[]>([])
const selected = ref('')

async function handleGenerate() {
  loading.value = true
  candidates.value = []
  try {
    candidates.value = await props.generateFn()
    if (candidates.value.length > 0) {
      selected.value = candidates.value[0]
    }
  } finally {
    loading.value = false
  }
}

function handleUse() {
  if (selected.value) {
    emit('use', selected.value)
  }
}

function onShow(show: boolean) {
  if (show && candidates.value.length === 0 && !loading.value) {
    handleGenerate()
  }
}
</script>

<style scoped>
.ai-candidates {
  padding: 4px 0;
}
</style>
