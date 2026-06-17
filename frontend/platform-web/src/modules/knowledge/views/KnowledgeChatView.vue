<script setup lang="ts">
import { ref, computed } from 'vue'
import { marked } from 'marked'
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

const renderedAnswer = computed(() => {
  if (!answer.value) return ''
  return marked.parse(answer.value) as string
})

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
        <div class="markdown-body" v-html="renderedAnswer"></div>
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

<style scoped>
.markdown-body {
  color: var(--text-primary);
  line-height: 1.8;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.markdown-body :deep(p) {
  margin-bottom: 0.75em;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 0.75em;
}

.markdown-body :deep(li) {
  margin-bottom: 0.25em;
}

.markdown-body :deep(code) {
  background: var(--code-bg, rgba(0, 0, 0, 0.06));
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: var(--code-bg, rgba(0, 0, 0, 0.06));
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 0.75em;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid var(--primary-color, #2080f0);
  padding-left: 1em;
  margin-left: 0;
  margin-bottom: 0.75em;
  color: var(--text-secondary);
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  margin-bottom: 0.75em;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--border-color, #e0e0e6);
  padding: 0.5em 0.75em;
}

.markdown-body :deep(th) {
  background: var(--code-bg, rgba(0, 0, 0, 0.06));
}
</style>
