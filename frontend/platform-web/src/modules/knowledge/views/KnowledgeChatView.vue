<script setup lang="ts">
import { ref } from 'vue'
import {
  NCard,
  NInput,
  NButton,
  NSpace,
  NTag,
  NCollapse,
  NCollapseItem,
  NSpin,
  useMessage
} from 'naive-ui'
import { queryKnowledge } from '@/api/knowledge'
import type { SourceInfo } from '@/api/knowledge'

const message = useMessage()
const loading = ref(false)
const question = ref('')
const answer = ref('')
const sources = ref<SourceInfo[]>([])

async function handleQuery() {
  if (!question.value.trim()) {
    message.warning('请输入问题')
    return
  }

  loading.value = true
  answer.value = ''
  sources.value = []

  try {
    const { data } = await queryKnowledge({
      question: question.value,
      topK: 5
    })
    answer.value = data.data.answer
    sources.value = data.data.sources || []
  } catch (error) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleQuery()
  }
}
</script>

<template>
  <div>
    <h2 class="text-lg font-semibold mb-6" style="color: var(--text-primary)">知识问答</h2>

    <!-- 输入区域 -->
    <div class="mb-6">
      <NSpace>
        <NInput
          v-model:value="question"
          placeholder="输入你的问题，基于知识库内容回答..."
          clearable
          size="large"
          class="w-96"
          @keydown="handleKeydown"
        />
        <NButton type="primary" size="large" :loading="loading" @click="handleQuery">
          提问
        </NButton>
      </NSpace>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-12">
      <NSpin size="large" description="正在查询知识库..." />
    </div>

    <!-- 回答区域 -->
    <div v-if="answer && !loading">
      <NCard title="回答" class="mb-6">
        <div class="whitespace-pre-wrap" style="color: var(--text-primary)">{{ answer }}</div>
      </NCard>

      <!-- 来源信息 -->
      <NCard v-if="sources.length > 0" title="参考来源">
        <NCollapse>
          <NCollapseItem
            v-for="(source, index) in sources"
            :key="index"
            :title="`${source.documentTitle}`"
            :name="String(index)"
          >
            <template #header-extra>
              <NTag size="small" type="info">
                相似度: {{ (source.similarity * 100).toFixed(1) }}%
              </NTag>
            </template>
            <div class="text-sm whitespace-pre-wrap" style="color: var(--text-secondary)">
              {{ source.chunkContent }}
            </div>
          </NCollapseItem>
        </NCollapse>
      </NCard>
    </div>

    <!-- 空状态 -->
    <div v-if="!answer && !loading" class="text-center py-12" style="color: var(--text-muted)">
      <p class="text-lg mb-2">基于知识库的智能问答</p>
      <p class="text-sm">请先上传文档到知识库，然后在这里提问</p>
    </div>
  </div>
</template>
