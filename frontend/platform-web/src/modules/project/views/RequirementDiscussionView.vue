<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { marked } from 'marked'
import {
  NCard,
  NInput,
  NButton,
  NSpace,
  NTag,
  NSelect,
  NAlert,
  NProgress,
  useMessage
} from 'naive-ui'
import request from '@/utils/request'
import type { ApiResult } from '@/types/api'
import { getProjectPage } from '@/api/project'
import type { ProjectVO } from '@/api/project'

const message = useMessage()
const loading = ref(false)
const sending = ref(false)
const generating = ref(false)

// 项目选择
const projects = ref<ProjectVO[]>([])
const selectedProjectId = ref<number | null>(null)
const projectOptions = computed(() =>
  projects.value.map(p => ({ label: p.name, value: p.id }))
)

// 讨论状态
const sessionId = ref<string | null>(null)
const initialRequirement = ref('')
const inputMessage = ref('')
const messages = ref<Array<{ role: string; content: string; timestamp: number }>>([])

// 需求完整性分析
const completeness = ref<{
  functionScore: number
  technicalScore: number
  boundaryScore: number
  nonFunctionScore: number
  overallScore: number
  missingItems: string[]
  suggestions: string[]
} | null>(null)

// 生成的文档
const generatedDocument = ref<string | null>(null)

// 消息容器引用
const messagesRef = ref<HTMLElement>()

// 加载项目列表
async function loadProjects() {
  try {
    const { data } = await getProjectPage({ current: 1, size: 100 })
    projects.value = data.data?.records || []
  } catch {
    projects.value = []
  }
}

// 开始讨论
async function handleStartDiscussion() {
  if (!selectedProjectId.value) {
    message.warning('请选择项目')
    return
  }
  if (!initialRequirement.value.trim()) {
    message.warning('请输入初始需求描述')
    return
  }

  loading.value = true
  try {
    const { data } = await request.post<ApiResult<{ sessionId: string; message: string }>>(
      '/requirement/discussions/start',
      null,
      {
        params: {
          projectId: selectedProjectId.value,
          initialRequirement: initialRequirement.value
        }
      }
    )

    sessionId.value = data.data.sessionId

    // 添加初始消息
    messages.value = [
      { role: 'user', content: initialRequirement.value, timestamp: Date.now() },
      { role: 'assistant', content: data.data.message, timestamp: Date.now() }
    ]

    message.success('需求讨论已开始')
    scrollToBottom()
  } catch {
    // interceptor handles error message
  } finally {
    loading.value = false
  }
}

// 继续讨论
async function handleSendMessage() {
  if (!sessionId.value || !inputMessage.value.trim()) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage,
    timestamp: Date.now()
  })
  scrollToBottom()

  sending.value = true
  try {
    const { data } = await request.post<ApiResult<{ response: string }>>(
      `/requirement/discussions/${sessionId.value}/continue`,
      null,
      { params: { message: userMessage } }
    )

    // 添加 Agent 回复
    messages.value.push({
      role: 'assistant',
      content: data.data.response,
      timestamp: Date.now()
    })
    scrollToBottom()
  } catch {
    // interceptor handles error message
  } finally {
    sending.value = false
  }
}

// 分析需求完整性
async function handleAnalyze() {
  if (!sessionId.value) return

  loading.value = true
  try {
    const { data } = await request.get<ApiResult<any>>(
      `/requirement/discussions/${sessionId.value}/analyze`
    )

    // 解析分析结果
    try {
      const analysisStr = data.data.analysis
      const jsonMatch = analysisStr.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        completeness.value = JSON.parse(jsonMatch[0])
      }
    } catch {
      message.warning('解析分析结果失败')
    }
  } catch {
    // interceptor handles error message
  } finally {
    loading.value = false
  }
}

// 生成需求文档
async function handleGenerateDocument() {
  if (!sessionId.value) return

  generating.value = true
  try {
    const { data } = await request.post<ApiResult<{ document: string }>>(
      `/requirement/discussions/${sessionId.value}/generate-document`
    )

    generatedDocument.value = data.data.document
    message.success('需求文档已生成')
  } catch {
    // interceptor handles error message
  } finally {
    generating.value = false
  }
}

// 渲染 Markdown
function renderMarkdown(content: string): string {
  if (!content) return ''
  return marked.parse(content) as string
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 获取分数颜色
function getScoreColor(score: number): 'success' | 'warning' | 'error' {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'error'
}

onMounted(() => {
  loadProjects()
})
</script>

<template>
  <div class="requirement-discussion">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">需求讨论</h2>
    </div>

    <!-- 项目选择和初始需求 -->
    <NCard v-if="!sessionId" class="mb-6">
      <template #header>
        <span class="text-sm font-medium" style="color: var(--text-secondary)">开始需求讨论</span>
      </template>

      <div class="mb-4">
        <span class="text-sm font-medium mb-2 block">选择项目 *</span>
        <NSelect
          v-model:value="selectedProjectId"
          :options="projectOptions"
          placeholder="请选择项目"
          filterable
        />
      </div>

      <div class="mb-4">
        <span class="text-sm font-medium mb-2 block">初始需求描述 *</span>
        <NInput
          v-model:value="initialRequirement"
          type="textarea"
          placeholder="请描述您的需求，包括：&#10;1. 想要实现什么功能&#10;2. 解决什么问题&#10;3. 预期效果"
          :rows="6"
        />
      </div>

      <NAlert type="info" class="mb-4">
        <p class="text-sm">
          <strong>提示：</strong>Agent 会读取项目信息，结合项目技术栈和架构，引导您进一步明确需求。
          请尽量详细描述，以便 Agent 给出更准确的建议。
        </p>
      </NAlert>

      <NSpace justify="end">
        <NButton type="primary" :loading="loading" @click="handleStartDiscussion">
          开始讨论
        </NButton>
      </NSpace>
    </NCard>

    <!-- 讨论区域 -->
    <div v-if="sessionId" class="discussion-area">
      <div class="flex gap-4">
        <!-- 左侧：对话区域 -->
        <div class="flex-1">
          <NCard class="chat-card">
            <template #header>
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium" style="color: var(--text-secondary)">需求讨论</span>
                <NSpace>
                  <NButton size="small" @click="handleAnalyze" :loading="loading">
                    分析完整性
                  </NButton>
                  <NButton size="small" type="primary" @click="handleGenerateDocument" :loading="generating">
                    生成需求文档
                  </NButton>
                </NSpace>
              </div>
            </template>

            <!-- 消息列表 -->
            <div ref="messagesRef" class="messages-container">
              <div
                v-for="(msg, index) in messages"
                :key="index"
                :class="['message', msg.role]"
              >
                <div class="message-header">
                  <NTag :type="msg.role === 'user' ? 'info' : 'success'" size="small">
                    {{ msg.role === 'user' ? '我' : 'Agent' }}
                  </NTag>
                </div>
                <div class="message-content" v-html="renderMarkdown(msg.content)" />
              </div>
            </div>

            <!-- 输入区域 -->
            <div class="input-area">
              <NInput
                v-model:value="inputMessage"
                type="textarea"
                placeholder="输入您的回复... (Ctrl+Enter 发送)"
                :rows="3"
                @keydown.ctrl.enter="handleSendMessage"
                :disabled="sending"
              />
              <div class="flex justify-end mt-2">
                <NButton
                  type="primary"
                  :loading="sending"
                  :disabled="!inputMessage.trim()"
                  @click="handleSendMessage"
                >
                  发送
                </NButton>
              </div>
            </div>
          </NCard>
        </div>

        <!-- 右侧：分析结果 -->
        <div class="w-80">
          <!-- 完整性分析 -->
          <NCard v-if="completeness" class="mb-4">
            <template #header>
              <span class="text-sm font-medium" style="color: var(--text-secondary)">需求完整性分析</span>
            </template>

            <div class="mb-3">
              <div class="flex justify-between mb-1">
                <span class="text-sm">功能完整性</span>
                <span class="text-sm">{{ completeness.functionScore }}%</span>
              </div>
              <NProgress
                :percentage="completeness.functionScore"
                :color="getScoreColor(completeness.functionScore)"
                :show-indicator="false"
                :height="8"
              />
            </div>

            <div class="mb-3">
              <div class="flex justify-between mb-1">
                <span class="text-sm">技术可行性</span>
                <span class="text-sm">{{ completeness.technicalScore }}%</span>
              </div>
              <NProgress
                :percentage="completeness.technicalScore"
                :color="getScoreColor(completeness.technicalScore)"
                :show-indicator="false"
                :height="8"
              />
            </div>

            <div class="mb-3">
              <div class="flex justify-between mb-1">
                <span class="text-sm">边界情况</span>
                <span class="text-sm">{{ completeness.boundaryScore }}%</span>
              </div>
              <NProgress
                :percentage="completeness.boundaryScore"
                :color="getScoreColor(completeness.boundaryScore)"
                :show-indicator="false"
                :height="8"
              />
            </div>

            <div class="mb-3">
              <div class="flex justify-between mb-1">
                <span class="text-sm">非功能需求</span>
                <span class="text-sm">{{ completeness.nonFunctionScore }}%</span>
              </div>
              <NProgress
                :percentage="completeness.nonFunctionScore"
                :color="getScoreColor(completeness.nonFunctionScore)"
                :show-indicator="false"
                :height="8"
              />
            </div>

            <div class="mt-4">
              <span class="text-sm font-medium">总分：</span>
              <NTag :type="getScoreColor(completeness.overallScore)" size="large">
                {{ completeness.overallScore }}%
              </NTag>
            </div>

            <div v-if="completeness.missingItems?.length" class="mt-4">
              <span class="text-sm font-medium block mb-2">待补充项：</span>
              <div v-for="(item, index) in completeness.missingItems" :key="index" class="text-sm text-gray-500 mb-1">
                • {{ item }}
              </div>
            </div>

            <div v-if="completeness.suggestions?.length" class="mt-4">
              <span class="text-sm font-medium block mb-2">建议：</span>
              <div v-for="(suggestion, index) in completeness.suggestions" :key="index" class="text-sm text-gray-500 mb-1">
                • {{ suggestion }}
              </div>
            </div>
          </NCard>

          <!-- 操作提示 -->
          <NCard>
            <template #header>
              <span class="text-sm font-medium" style="color: var(--text-secondary)">操作指南</span>
            </template>
            <div class="text-sm" style="color: var(--text-secondary)">
              <p class="mb-2">1. 与 Agent 讨论需求细节</p>
              <p class="mb-2">2. 点击"分析完整性"查看需求完整度</p>
              <p class="mb-2">3. 当完整度达到 80% 以上时，可生成需求文档</p>
              <p>4. 生成的需求文档可用于后续任务拆分</p>
            </div>
          </NCard>
        </div>
      </div>

      <!-- 生成的需求文档 -->
      <NCard v-if="generatedDocument" class="mt-6">
        <template #header>
          <span class="text-sm font-medium" style="color: var(--text-secondary)">生成的需求文档</span>
        </template>
        <div class="document-content" v-html="renderMarkdown(generatedDocument)" />
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.requirement-discussion {
  height: 100%;
}

.discussion-area {
  height: calc(100vh - 200px);
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card :deep(.n-card__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: calc(100vh - 400px);
}

.message {
  margin-bottom: 16px;
}

.message-header {
  margin-bottom: 8px;
}

.message-content {
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-content {
  background: var(--accent-soft);
  margin-left: 40px;
}

.message.assistant .message-content {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  margin-right: 40px;
}

.message-content :deep(p) {
  margin: 0 0 8px 0;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(code) {
  background: var(--hover-bg);
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 13px;
}

.message-content :deep(pre) {
  background: var(--hover-bg);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.input-area {
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.document-content {
  max-height: 500px;
  overflow-y: auto;
}

.document-content :deep(h1),
.document-content :deep(h2),
.document-content :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
}

.document-content :deep(ul),
.document-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 12px;
}

.document-content :deep(li) {
  margin-bottom: 4px;
}
</style>
