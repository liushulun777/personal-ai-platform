# AI 功能设计 - 博客前端对接

## 文档信息

| 项目 | 内容 |
|------|------|
| 版本 | v1.0 |
| 日期 | 2026-06-13 |
| 状态 | 设计中 |
| 范围 | blog-web + platform-web |

---

## 一、现状分析

### 已有后端 API

| API | 端点 | 状态 |
|-----|------|------|
| AI 摘要生成 | `POST /api/ai/generate/summary` | 已实现 |
| AI 标签生成 | `POST /api/ai/generate/tags` | 已实现 |
| AI 标题生成 | `POST /api/ai/generate/titles` | 已实现 |
| AI 对话 | `POST /api/ai/conversations/chat` | 已实现 |
| 对话列表 | `GET /api/ai/conversations` | 已实现 |
| 对话详情 | `GET /api/ai/conversations/{id}` | 已实现 |
| 删除对话 | `DELETE /api/ai/conversations/{id}` | 已实现 |
| 模型列表 | `GET /api/ai/models` | 已实现 |

### 前端现状

- **blog-web**: 零 AI 集成，纯展示
- **platform-web**: 零 AI 集成，文章编辑器无 AI 辅助

---

## 二、功能规划

### Phase 1: 管理端 AI 辅助写作（platform-web）

#### 2.1 文章编辑器 AI 集成

**场景**: 编辑文章时，AI 辅助生成摘要、标签、标题

**交互设计**:

```
┌─────────────────────────────────────────────────┐
│  文章编辑器                                      │
├─────────────────────────────────────────────────┤
│  标题: [________________________] [AI生成标题]    │
│                                                 │
│  分类: [选择分类▼]                               │
│  标签: [tag1] [tag2] [+添加]  [AI推荐标签]       │
│                                                 │
│  封面: [上传]                                    │
│                                                 │
│  摘要: [________________________] [AI生成摘要]    │
│                                                 │
│  ┌─────────────────────────────────────────┐    │
│  │                                         │    │
│  │         Markdown 编辑器                 │    │
│  │                                         │    │
│  └─────────────────────────────────────────┘    │
│                                                 │
│  [保存草稿]  [预览]  [发布]                       │
└─────────────────────────────────────────────────┘
```

**功能点**:

| 功能 | 按钮位置 | 触发条件 | 行为 |
|------|---------|---------|------|
| AI 生成标题 | 标题输入框右侧 | 需要已有正文内容 | 生成 3 个候选标题，点击填入 |
| AI 推荐标签 | 标签区域右侧 | 需要已有标题+正文 | 生成 5 个标签建议，点击添加 |
| AI 生成摘要 | 摘要输入框右侧 | 需要已有正文内容 | 生成摘要，可覆盖或追加 |

**UI 组件**:

```vue
<!-- AI 生成按钮组件 -->
<template>
  <n-button
    :loading="loading"
    :disabled="disabled"
    size="small"
    type="primary"
    ghost
    @click="handleGenerate"
  >
    <template #icon>
      <n-icon><SparkleIcon /></n-icon>
    </template>
    {{ label }}
  </n-button>
</template>
```

**候选结果弹窗**:

```
┌──────────────────────────────────────┐
│  AI 生成标题                    [✕]  │
├──────────────────────────────────────┤
│                                      │
│  ○ Spring Boot 入门指南：从零开始     │
│  ○ 一文掌握 Spring Boot 核心概念      │
│  ○ Spring Boot 实战教程              │
│                                      │
│  [重新生成]              [使用选中]   │
└──────────────────────────────────────┘
```

#### 2.2 AI 助手侧边栏

**场景**: 写作过程中随时调用 AI 对话

**交互设计**:

```
┌────────────────────────┬──────────────────┐
│                        │  AI 助手    [✕]  │
│                        ├──────────────────┤
│                        │  [当前模型: mimo▼]│
│     文章编辑器          │                  │
│                        │  帮我优化这段代码  │
│                        │  ↓               │
│                        │  以下是优化建议... │
│                        │                  │
│                        │  ──────────────  │
│                        │  [输入消息...]    │
└────────────────────────┴──────────────────┘
```

**功能点**:

| 功能 | 说明 |
|------|------|
| 侧边栏切换 | 右侧抽屉，可收起 |
| 对话管理 | 新建对话、历史对话列表 |
| 上下文感知 | 可选将当前文章内容作为上下文 |
| 模型切换 | 选择 mimo / openai |
| Markdown 渲染 | AI 回复支持 Markdown |

---

### Phase 2: 公开博客 AI 功能（blog-web）

#### 2.3 AI 文章问答

**场景**: 读者在文章详情页针对文章内容提问

**交互设计**:

```
┌─────────────────────────────────────────────────┐
│  [文章内容区域...]                               │
│                                                 │
├─────────────────────────────────────────────────┤
│                                                 │
│  💬 AI 文章问答                                  │
│  ┌─────────────────────────────────────────┐    │
│  │ 针对这篇文章，你可以问我任何问题...       │    │
│  └─────────────────────────────────────────┘    │
│                                                 │
│  Q: 这篇文章的核心观点是什么？                    │
│  A: 本文核心观点是...                           │
│                                                 │
│  Q: 能举个实际应用的例子吗？                     │
│  A: 当然，比如...                               │
│                                                 │
│  [输入你的问题________________] [发送]           │
│                                                 │
└─────────────────────────────────────────────────┘
```

**后端需求**: 新增 API，将文章内容作为 system prompt 上下文

#### 2.4 智能推荐

**场景**: 文章详情页底部推荐相关文章

**交互设计**:

```
┌─────────────────────────────────────────────────┐
│  📚 相关推荐                                    │
│                                                 │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │ 文章 A  │ │ 文章 B  │ │ 文章 C  │           │
│  │ 标签    │ │ 标签    │ │ 标签    │           │
│  │ 相似度  │ │ 相似度  │ │ 相似度  │           │
│  └─────────┘ └─────────┘ └─────────┘           │
└─────────────────────────────────────────────────┘
```

**后端需求**: 基于文章标签/内容的相似度推荐 API

---

## 三、API 对接设计

### 3.1 前端 API 模块

```typescript
// frontend/platform-web/src/api/ai.ts

import request from '@/utils/request'

// AI 生成摘要
export function generateSummary(content: string, maxLength?: number) {
  return request.post<string>('/api/ai/generate/summary', {
    content,
    maxLength: maxLength || 200,
  })
}

// AI 生成标签
export function generateTags(title: string, content: string, count?: number) {
  return request.post<string[]>('/api/ai/generate/tags', {
    title,
    content,
    count: count || 5,
  })
}

// AI 生成标题
export function generateTitles(content: string, count?: number) {
  return request.post<string[]>('/api/ai/generate/titles', {
    content,
    count: count || 3,
  })
}

// AI 对话
export function chat(message: string, conversationId?: number, model?: string) {
  return request.post<ChatVO>('/api/ai/conversations/chat', {
    message,
    conversationId,
    model,
  })
}

// 对话列表
export function getConversations(params: { current: number; size: number }) {
  return request.get<PageResult<ConversationListVO>>('/api/ai/conversations', { params })
}

// 对话详情
export function getConversationDetail(conversationId: number) {
  return request.get<ConversationVO>(`/api/ai/conversations/${conversationId}`)
}

// 删除对话
export function deleteConversation(conversationId: number) {
  return request.delete(`/api/ai/conversations/${conversationId}`)
}

// 获取可用模型
export function getAvailableModels() {
  return request.get<ModelInfo[]>('/api/ai/models')
}
```

### 3.2 类型定义

```typescript
// frontend/platform-web/src/types/ai.d.ts

interface ChatDTO {
  message: string
  conversationId?: number
  model?: string
}

interface ChatVO {
  conversationId: number
  reply: string
  tokenCount: number
}

interface ConversationListVO {
  id: number
  title: string
  model: string
  lastMessage: string
  createTime: string
}

interface ConversationVO {
  id: number
  title: string
  model: string
  messages: MessageVO[]
  createTime: string
}

interface MessageVO {
  id: number
  role: 'system' | 'user' | 'assistant'
  content: string
  tokenCount: number
  createTime: string
}

interface ModelInfo {
  name: string
  model: string
  isDefault: string
}
```

---

## 四、组件设计

### 4.1 AI 生成按钮组件

```vue
<!-- components/ai/AiGenerateButton.vue -->
<template>
  <n-popover trigger="click" placement="bottom" :width="320">
    <template #trigger>
      <n-button
        :loading="loading"
        :disabled="disabled"
        size="small"
        type="primary"
        ghost
      >
        <template #icon>
          <n-icon><Sparkles /></n-icon>
        </template>
        {{ label }}
      </n-button>
    </template>

    <div class="ai-candidates">
      <n-radio-group v-model:value="selected" vertical>
        <n-radio
          v-for="(item, index) in candidates"
          :key="index"
          :value="item"
        >
          {{ item }}
        </n-radio>
      </n-radio-group>

      <div class="ai-actions">
        <n-button size="tiny" @click="handleRegenerate">重新生成</n-button>
        <n-button size="tiny" type="primary" @click="handleUse">使用</n-button>
      </div>
    </div>
  </n-popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Sparkles } from '@vicons/ionicons5'

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
const selected = ref<string>('')

async function handleGenerate() {
  loading.value = true
  try {
    candidates.value = await props.generateFn()
    if (candidates.value.length > 0) {
      selected.value = candidates.value[0]
    }
  } finally {
    loading.value = false
  }
}

function handleRegenerate() {
  handleGenerate()
}

function handleUse() {
  if (selected.value) {
    emit('use', selected.value)
  }
}
</script>
```

### 4.2 AI 聊天组件

```vue
<!-- components/ai/AiChatPanel.vue -->
<template>
  <div class="ai-chat-panel">
    <!-- 头部 -->
    <div class="chat-header">
      <span>AI 助手</span>
      <n-select
        v-model:value="currentModel"
        :options="modelOptions"
        size="tiny"
        style="width: 120px"
      />
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messagesRef">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message', msg.role]"
      >
        <div class="message-content" v-html="renderMarkdown(msg.content)" />
      </div>
      <div v-if="chatLoading" class="message assistant">
        <n-spin size="small" />
      </div>
    </div>

    <!-- 输入框 -->
    <div class="chat-input">
      <n-input
        v-model:value="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入消息..."
        @keydown.enter.ctrl="handleSend"
      />
      <n-button
        type="primary"
        :disabled="!inputMessage.trim()"
        :loading="chatLoading"
        @click="handleSend"
      >
        发送
      </n-button>
    </div>
  </div>
</template>
```

### 4.3 AI 文章问答组件

```vue
<!-- components/ai/AiArticleQA.vue -->
<template>
  <div class="ai-article-qa">
    <n-divider />
    <h3>💬 AI 文章问答</h3>
    <p class="qa-hint">针对这篇文章，你可以向 AI 提问</p>

    <!-- 历史问答 -->
    <div class="qa-history">
      <div v-for="(qa, index) in qaHistory" :key="index" class="qa-item">
        <div class="question">Q: {{ qa.question }}</div>
        <div class="answer" v-html="renderMarkdown(qa.answer)" />
      </div>
    </div>

    <!-- 输入 -->
    <div class="qa-input">
      <n-input
        v-model:value="question"
        placeholder="输入你的问题..."
        @keydown.enter="handleAsk"
      />
      <n-button
        type="primary"
        :loading="loading"
        :disabled="!question.trim()"
        @click="handleAsk"
      >
        提问
      </n-button>
    </div>
  </div>
</template>
```

---

## 五、后端扩展需求

### 5.1 新增 API: 文章问答

```
POST /api/ai/generate/ask
```

**Request Body**:
```json
{
  "articleId": 123,
  "question": "这篇文章的核心观点是什么？"
}
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "answer": "本文核心观点是...",
    "tokenCount": 150
  }
}
```

**实现逻辑**:
1. 从 blog-service 获取文章内容
2. 构建 system prompt: "你是文章助手，基于以下文章内容回答问题：\n\n{articleContent}"
3. 调用 AiService.chat(question, systemPrompt)

### 5.2 新增 API: 文章推荐

```
GET /api/ai/recommend/articles/{articleId}
```

**Response**:
```json
{
  "code": 200,
  "data": [
    {
      "articleId": 456,
      "title": "相关文章标题",
      "summary": "摘要...",
      "similarity": 0.85
    }
  ]
}
```

**实现逻辑**:
1. 获取当前文章的标签和分类
2. 查询同标签/分类的文章
3. 按标签重合度排序，返回 top 5

### 5.3 新增 API: Prompt 模板管理

```
GET    /api/ai/prompts              # 获取模板列表
POST   /api/ai/prompts              # 创建模板
PUT    /api/ai/prompts/{id}         # 更新模板
DELETE /api/ai/prompts/{id}         # 删除模板
```

---

## 六、菜单与路由扩展

### platform-web 新增菜单

```
├── Dashboard
├── 系统管理
│   ├── 用户管理
│   ├── 角色管理
│   └── 菜单管理
├── 博客管理
│   ├── 文章管理
│   ├── 分类管理
│   └── 标签管理
└── AI 助手          ← 新增
    ├── AI 对话      ← 新增
    └── Prompt 模板  ← 新增
```

### 新增路由

```typescript
// platform-web/src/router/index.ts 新增
{
  path: '/ai',
  children: [
    {
      path: 'chat',
      name: 'AiChat',
      component: () => import('@/modules/ai/views/AiChatView.vue'),
      meta: { title: 'AI 对话', icon: 'chat' }
    },
    {
      path: 'prompts',
      name: 'PromptTemplates',
      component: () => import('@/modules/ai/views/PromptListView.vue'),
      meta: { title: 'Prompt 模板', icon: 'prompt' }
    }
  ]
}
```

---

## 七、实施计划

### P0: 核心功能（第一阶段）

| 任务 | 前端 | 后端 | 工作量 |
|------|------|------|--------|
| AI 生成标题按钮 | platform-web | 已有 API | 0.5d |
| AI 推荐标签按钮 | platform-web | 已有 API | 0.5d |
| AI 生成摘要按钮 | platform-web | 已有 API | 0.5d |
| AI 对话页面 | platform-web | 已有 API | 2d |
| 对话列表页面 | platform-web | 已有 API | 1d |

### P1: 增强功能（第二阶段）

| 任务 | 前端 | 后端 | 工作量 |
|------|------|------|--------|
| AI 助手侧边栏 | platform-web | 已有 API | 2d |
| Prompt 模板管理 | platform-web | 需新增 | 2d |
| 文章问答功能 | blog-web | 需新增 | 2d |
| 模型切换功能 | 两端 | 已有 API | 1d |

### P2: 智能推荐（第三阶段）

| 任务 | 前端 | 后端 | 工作量 |
|------|------|------|--------|
| 文章智能推荐 | blog-web | 需新增 | 2d |
| RAG 知识库集成 | - | 需新增 | 5d |

---

## 八、技术注意事项

1. **流式响应**: AI 对话建议后续支持 SSE 流式输出，提升体验
2. **错误处理**: AI 服务可能超时，需设置合理超时时间和重试机制
3. **Token 计量**: 前端可展示 token 消耗，后端已有 tokenCount 字段
4. **上下文限制**: 对话历史过长时需截断，避免超出模型上下文窗口
5. **缓存策略**: 相同内容的摘要/标签生成可缓存，减少 API 调用
