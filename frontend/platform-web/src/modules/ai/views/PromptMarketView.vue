<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NCard,
  NGrid,
  NGi,
  NButton,
  NTag,
  NInput,
  NSelect,
  NSpace,
  NEmpty,
  NModal,
  useMessage
} from 'naive-ui'
import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

interface PromptTemplate {
  id: number
  name: string
  description: string
  promptText: string
  category: string
  usageCount: number
  authorName: string
  createTime: string
}

const message = useMessage()
const loading = ref(false)
const prompts = ref<PromptTemplate[]>([])
const searchKeyword = ref('')
const selectedCategory = ref<string | null>(null)
const showDetailModal = ref(false)
const currentPrompt = ref<PromptTemplate | null>(null)

const categoryOptions = [
  { label: '写作', value: 'writing' },
  { label: '编程', value: 'coding' },
  { label: '翻译', value: 'translation' },
  { label: '分析', value: 'analysis' },
  { label: '创意', value: 'creative' },
  { label: '其他', value: 'other' }
]

const categoryMap: Record<string, string> = {
  'writing': '写作',
  'coding': '编程',
  'translation': '翻译',
  'analysis': '分析',
  'creative': '创意',
  'other': '其他'
}

async function loadPrompts() {
  loading.value = true
  try {
    const { data } = await request.get<ApiResult<PromptTemplate[]>>('/ai/prompts/market', {
      params: {
        keyword: searchKeyword.value || undefined,
        category: selectedCategory.value || undefined
      }
    })
    prompts.value = data.data || []
  } catch {
    prompts.value = []
  } finally {
    loading.value = false
  }
}

function showDetail(prompt: PromptTemplate) {
  currentPrompt.value = prompt
  showDetailModal.value = true
}

async function handleCopy(prompt: PromptTemplate) {
  try {
    await navigator.clipboard.writeText(prompt.promptText)
    message.success('已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

function handleSearch() {
  loadPrompts()
}

onMounted(() => {
  loadPrompts()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">Prompt 市场</h2>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="searchKeyword"
        placeholder="搜索 Prompt..."
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NSelect
        v-model:value="selectedCategory"
        :options="categoryOptions"
        placeholder="分类"
        clearable
        size="small"
        class="w-32"
        @update:value="handleSearch"
      />
      <NButton size="small" type="primary" @click="handleSearch">搜索</NButton>
    </div>

    <!-- Prompt 列表 -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16">
      <NGi v-for="prompt in prompts" :key="prompt.id">
        <NCard hoverable @click="showDetail(prompt)">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium">{{ prompt.name }}</span>
              <NTag size="small" type="info">
                {{ categoryMap[prompt.category] || prompt.category }}
              </NTag>
            </div>
          </template>

          <p class="text-sm mb-3" style="color: var(--text-secondary); min-height: 40px">
            {{ prompt.description || '暂无描述' }}
          </p>

          <div class="flex items-center justify-between text-xs" style="color: var(--text-muted)">
            <span>使用次数: {{ prompt.usageCount || 0 }}</span>
            <span>{{ prompt.authorName || '匿名' }}</span>
          </div>

          <template #footer>
            <NSpace>
              <NButton size="tiny" type="primary" @click.stop="handleCopy(prompt)">
                复制
              </NButton>
              <NButton size="tiny" @click.stop="showDetail(prompt)">
                查看详情
              </NButton>
            </NSpace>
          </template>
        </NCard>
      </NGi>
    </NGrid>

    <div v-if="!loading && prompts.length === 0" class="mt-8">
      <NEmpty description="暂无 Prompt 模板" />
    </div>

    <!-- 详情弹窗 -->
    <NModal
      v-model:show="showDetailModal"
      title="Prompt 详情"
      preset="card"
      style="width: 600px"
    >
      <div v-if="currentPrompt">
        <div class="mb-4">
          <h3 class="text-lg font-medium mb-2">{{ currentPrompt.name }}</h3>
          <NTag size="small" type="info">
            {{ categoryMap[currentPrompt.category] || currentPrompt.category }}
          </NTag>
        </div>

        <div class="mb-4">
          <p class="text-sm" style="color: var(--text-secondary)">
            {{ currentPrompt.description }}
          </p>
        </div>

        <div class="mb-4">
          <span class="text-sm font-medium block mb-2">Prompt 内容：</span>
          <div class="prompt-content">
            {{ currentPrompt.promptText }}
          </div>
        </div>

        <div class="flex items-center justify-between text-xs" style="color: var(--text-muted)">
          <span>作者: {{ currentPrompt.authorName || '匿名' }}</span>
          <span>使用次数: {{ currentPrompt.usageCount || 0 }}</span>
        </div>
      </div>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="showDetailModal = false">关闭</NButton>
          <NButton type="primary" @click="currentPrompt && handleCopy(currentPrompt)">
            复制 Prompt
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.prompt-content {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}
</style>
