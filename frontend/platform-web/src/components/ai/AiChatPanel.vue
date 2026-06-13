<template>
  <div class="ai-chat-panel">
    <!-- 头部 -->
    <div class="chat-header">
      <span class="chat-title">AI 助手</span>
      <n-select
        v-model:value="currentModel"
        :options="modelOptions"
        size="tiny"
        style="width: 120px"
        placeholder="选择模型"
      />
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="empty-state">
        <n-icon size="48" color="#ccc"><chatbubbles-outline /></n-icon>
        <p>开始与 AI 对话</p>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="msg.id"
        :class="['message', msg.role]"
      >
        <div class="message-avatar">
          <n-icon v-if="msg.role === 'user'" size="20"><person-outline /></n-icon>
          <n-icon v-else size="20"><sparkles-outline /></n-icon>
        </div>
        <div class="message-content">
          <!-- 流式加载中且内容为空时显示 spinner -->
          <div v-if="chatLoading && msg.role === 'assistant' && !msg.content" class="message-text">
            <n-spin size="small" />
          </div>
          <!-- 流式加载中有内容时：简单换行显示 -->
          <div v-else-if="chatLoading && msg.role === 'assistant' && streamingMsgId === msg.id" class="message-text streaming" v-html="renderStreamingContent(msg.content)" />
          <!-- 完成后：markdown 渲染 -->
          <div v-else class="message-text" v-html="renderMarkdown(msg.content)" />
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="chat-input">
      <!-- 已选 Prompt 标签 -->
      <div v-if="selectedPrompt" class="selected-prompt">
        <n-tag size="small" type="info" closable @close="clearPrompt">
          {{ selectedPrompt.name }}
        </n-tag>
        <span class="prompt-hint">{{ getPromptHint(selectedPrompt) }}</span>
      </div>
      <n-input
        v-model:value="inputMessage"
        type="textarea"
        :rows="2"
        :placeholder="inputPlaceholder"
        @keydown.ctrl.enter="handleSend"
        :disabled="chatLoading"
      />
      <div class="chat-actions">
        <PromptSelector @select="handlePromptSelect" />
        <div class="actions-right">
          <n-button size="tiny" @click="handleClear">清空对话</n-button>
          <n-button
            type="primary"
            size="tiny"
            :disabled="!inputMessage.trim()"
            :loading="chatLoading"
            @click="handleSend"
          >
            发送
          </n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { marked } from 'marked'
import {
  ChatbubblesOutline,
  PersonOutline,
  SparklesOutline,
} from '@vicons/ionicons5'
import { chatStream, getAvailableModels, getConversationDetail } from '@/api/ai'
import PromptSelector from './PromptSelector.vue'
import type { MessageVO, ModelInfo, PromptVO } from '@/types/ai'

const emit = defineEmits<{
  (e: 'conversationCreated', conversationId: string): void
}>()

const messages = ref<MessageVO[]>([])
const inputMessage = ref('')
const chatLoading = ref(false)
const conversationId = ref<number | undefined>()
const currentModel = ref('mimo')
const modelOptions = ref<{ label: string; value: string }[]>([])
const messagesRef = ref<HTMLElement>()
const streamingMsgId = ref<number | null>(null)
const selectedPrompt = ref<PromptVO | null>(null)

onMounted(async () => {
  try {
    const res = await getAvailableModels()
    const models = (res as any).data?.data || []
    modelOptions.value = models.map((m: ModelInfo) => ({
      label: m.name,
      value: m.name,
    }))
  } catch {
    modelOptions.value = [{ label: 'mimo', value: 'mimo' }]
  }
})

function renderMarkdown(content: string): string {
  if (!content) return ''
  return marked.parse(content, { breaks: true }) as string
}

// 流式内容渲染：简单处理换行和代码块
function renderStreamingContent(content: string): string {
  if (!content) return ''
  // 转义 HTML
  let escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  // 换行转 <br>
  escaped = escaped.replace(/\n/g, '<br>')
  return escaped
}

async function handleSend() {
  const userInput = inputMessage.value.trim()
  if (!userInput || chatLoading.value) return

  // 如果有选中的 Prompt，组合模板和用户输入
  let finalMessage = userInput
  let displayMessage = userInput

  if (selectedPrompt.value) {
    const template = selectedPrompt.value.promptText
    // 将所有 {{xxx}} 占位符替换为用户输入
    finalMessage = template.replace(/\{\{\w+\}\}/g, userInput)
    // 显示消息：显示 Prompt 名称 + 用户输入
    displayMessage = `[${selectedPrompt.value.name}] ${userInput}`
  }

  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: displayMessage,
    tokenCount: 0,
    createTime: new Date().toISOString(),
  })
  inputMessage.value = ''
  clearPrompt()
  scrollToBottom()

  // 添加空的 AI 消息占位
  const aiMsgIndex = messages.value.length
  const aiMsgId = Date.now() + 1
  messages.value.push({
    id: aiMsgId,
    role: 'assistant',
    content: '',
    tokenCount: 0,
    createTime: new Date().toISOString(),
  })

  chatLoading.value = true
  streamingMsgId.value = aiMsgId
  let fullReply = ''

  await chatStream(
    {
      message: finalMessage,
      conversationId: conversationId.value,
      model: currentModel.value,
    },
    // onChunk - 每收到一个片段就更新消息
    (text: string) => {
      // 处理 conversationId 事件
      if (text.startsWith('conversationId:')) {
        const newId = text.split(':')[1]
        conversationId.value = Number(newId)
        emit('conversationCreated', newId)
        return
      }
      fullReply += text
      messages.value[aiMsgIndex].content = fullReply
      scrollToBottom()
    },
    // onDone - 流式完成
    () => {
      chatLoading.value = false
      streamingMsgId.value = null
      scrollToBottom()
    },
    // onError - 错误处理
    (error: Error) => {
      if (!fullReply) {
        messages.value[aiMsgIndex].content = `抱歉，发生了错误: ${error.message || '未知错误'}`
      }
      chatLoading.value = false
      streamingMsgId.value = null
      scrollToBottom()
    }
  )
}

function handleClear() {
  messages.value = []
  conversationId.value = undefined
  clearPrompt()
}

function handlePromptSelect(prompt: PromptVO) {
  selectedPrompt.value = prompt
  inputMessage.value = ''
}

function clearPrompt() {
  selectedPrompt.value = null
}

// 根据 Prompt 模板生成输入提示
function getPromptHint(prompt: PromptVO): string {
  const placeholders = prompt.promptText.match(/\{\{(\w+)\}\}/g)
  if (!placeholders) return '请输入内容'
  const names = placeholders.map(p => {
    const key = p.replace(/\{\{|\}\}/g, '')
    const labelMap: Record<string, string> = {
      content: '内容',
      title: '标题',
      code: '代码',
      language: '编程语言',
      error: '错误信息',
      product: '产品名称',
      features: '产品特点',
      platform: '平台',
      topic: '主题',
      subject: '邮件主题',
      recipient: '收件人',
      points: '要点',
      question: '问题',
      role: '角色',
      target_language: '目标语言'
    }
    return labelMap[key] || key
  })
  return `请填写：${names.join('、')}`
}

// 输入框占位符
const inputPlaceholder = computed(() => {
  if (selectedPrompt.value) {
    return getPromptHint(selectedPrompt.value)
  }
  return '输入消息... (Ctrl+Enter 发送)'
})

async function loadConversation(id: number) {
  chatLoading.value = true
  try {
    const res = await getConversationDetail(id)
    const detail = (res as any).data?.data
    conversationId.value = id
    messages.value = (detail?.messages || []).map((m: any) => ({
      id: m.id,
      role: m.role,
      content: m.content,
      tokenCount: m.tokenCount || 0,
      createTime: m.createTime,
    }))
    scrollToBottom()
  } catch (error: any) {
    messages.value = []
  } finally {
    chatLoading.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

defineExpose({ handleClear, loadConversation })
</script>

<style scoped>
.ai-chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-card);
}

.chat-title {
  font-weight: 600;
  font-size: 14px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 300px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
}

.empty-state p {
  margin-top: 8px;
  font-size: 13px;
}

.message {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent);
  color: white;
}

.message.user .message-avatar {
  background: var(--text-muted);
}

.message-content {
  max-width: 80%;
}

.message-text {
  padding: 8px 12px;
  border-radius: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-primary);
}

.message.user .message-text {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
}

.message-text :deep(p) {
  margin: 0 0 8px 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(code) {
  background: var(--hover-bg);
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 12px;
}

.message-text :deep(pre) {
  background: var(--hover-bg);
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.selected-prompt {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.prompt-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.chat-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.actions-right {
  display: flex;
  gap: 8px;
}
</style>
