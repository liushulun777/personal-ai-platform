<template>
  <n-popover trigger="click" placement="top" :width="360" @update:show="onShow">
    <template #trigger>
      <n-button size="tiny" quaternary>
        <template #icon>
          <n-icon><book-outline /></n-icon>
        </template>
        Prompt
      </n-button>
    </template>

    <div class="prompt-selector">
      <!-- 分类标签 -->
      <div class="category-tabs">
        <n-tag
          v-for="(count, cat) in categories"
          :key="cat"
          :type="selectedCategory === cat ? 'primary' : 'default'"
          size="small"
          checkable
          :checked="selectedCategory === cat"
          @update:checked="selectCategory(cat)"
        >
          {{ categoryLabels[cat as string] || cat }} ({{ count }})
        </n-tag>
      </div>

      <!-- 模板列表 -->
      <div class="prompt-list">
        <div v-if="loading" class="loading-state">
          <n-spin size="small" />
        </div>
        <div v-else-if="prompts.length === 0" class="empty-state">
          暂无模板
        </div>
        <div
          v-for="prompt in prompts"
          :key="prompt.id"
          class="prompt-item"
          @click="handleSelect(prompt)"
        >
          <div class="prompt-name">{{ prompt.name }}</div>
          <div class="prompt-desc">{{ prompt.description }}</div>
        </div>
      </div>
    </div>
  </n-popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NIcon, NPopover, NTag, NSpin } from 'naive-ui'
import { BookOutline } from '@vicons/ionicons5'
import { getPromptsByCategory, getPromptCategories } from '@/api/ai'
import type { PromptVO } from '@/types/ai'

const emit = defineEmits<{
  (e: 'select', prompt: PromptVO): void
}>()

const loading = ref(false)
const categories = ref<Record<string, number>>({})
const selectedCategory = ref<string>('')
const prompts = ref<PromptVO[]>([])

const categoryLabels: Record<string, string> = {
  writing: '写作',
  code: '代码',
  translation: '翻译',
  summary: '总结',
  marketing: '营销',
  chat: '对话',
  other: '其他'
}

async function loadCategories() {
  try {
    const res = await getPromptCategories()
    categories.value = (res as any).data?.data || {}
    // 默认选中第一个分类
    const firstCat = Object.keys(categories.value)[0]
    if (firstCat) {
      selectCategory(firstCat)
    }
  } catch {
    categories.value = {}
  }
}

async function selectCategory(category: string) {
  selectedCategory.value = category
  loading.value = true
  try {
    const res = await getPromptsByCategory(category)
    prompts.value = (res as any).data?.data || []
  } catch {
    prompts.value = []
  } finally {
    loading.value = false
  }
}

function handleSelect(prompt: PromptVO) {
  emit('select', prompt)
}

function onShow(show: boolean) {
  if (show && Object.keys(categories.value).length === 0) {
    loadCategories()
  }
}
</script>

<style scoped>
.prompt-selector {
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 8px;
}

.prompt-list {
  flex: 1;
  overflow-y: auto;
  max-height: 300px;
}

.loading-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: var(--text-muted);
  font-size: 13px;
}

.prompt-item {
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.prompt-item:hover {
  background: var(--hover-bg);
}

.prompt-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.prompt-desc {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
