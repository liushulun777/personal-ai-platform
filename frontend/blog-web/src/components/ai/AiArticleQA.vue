<template>
  <div class="ai-article-qa">
    <div class="qa-header">
      <span class="qa-icon">💬</span>
      <h3>AI 文章问答</h3>
    </div>
    <p class="qa-hint">针对这篇文章，你可以向 AI 提问</p>

    <!-- 历史问答 -->
    <div v-if="qaHistory.length > 0" class="qa-history">
      <div v-for="(qa, index) in qaHistory" :key="index" class="qa-item">
        <div class="question">
          <span class="q-label">Q:</span>
          {{ qa.question }}
        </div>
        <div class="answer">
          <span class="a-label">A:</span>
          <div class="answer-content" v-html="renderMarkdown(qa.answer)" />
        </div>
      </div>
    </div>

    <!-- 输入 -->
    <div class="qa-input">
      <NInput
        v-model:value="question"
        placeholder="输入你的问题..."
        :disabled="loading"
        @keydown.enter="handleAsk"
      />
      <NButton
        type="primary"
        size="small"
        :loading="loading"
        :disabled="!question.trim()"
        @click="handleAsk"
      >
        提问
      </NButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NInput, NButton, useMessage } from 'naive-ui'
import { marked } from 'marked'
import { askArticleQuestion } from '@/api/ai'

interface Props {
  articleId: string
}

const props = defineProps<Props>()
const message = useMessage()

const question = ref('')
const loading = ref(false)
const qaHistory = ref<{ question: string; answer: string }[]>([])

function renderMarkdown(content: string): string {
  return marked.parse(content, { breaks: true }) as string
}

async function handleAsk() {
  const q = question.value.trim()
  if (!q || loading.value) return

  loading.value = true
  try {
    const { data } = await askArticleQuestion(props.articleId, q)
    qaHistory.value.push({
      question: q,
      answer: data.data?.answer || data.answer || '抱歉，无法获取回答',
    })
    question.value = ''
  } catch (error: any) {
    message.error(error.message || '提问失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-article-qa {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.qa-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.qa-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.qa-icon {
  font-size: 18px;
}

.qa-hint {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.qa-history {
  margin-bottom: 16px;
}

.qa-item {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 8px;
  background: var(--hover-bg);
  border: 1px solid var(--border-color);
}

.question {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.q-label {
  color: var(--accent);
  font-weight: 600;
  margin-right: 4px;
}

.answer {
  font-size: 14px;
  color: var(--text-secondary);
}

.a-label {
  color: var(--accent);
  font-weight: 600;
  margin-right: 4px;
}

.answer-content {
  display: inline;
}

.answer-content :deep(p) {
  margin: 4px 0;
}

.answer-content :deep(code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 12px;
}

.qa-input {
  display: flex;
  gap: 8px;
}

.qa-input :deep(.n-input) {
  flex: 1;
}
</style>
