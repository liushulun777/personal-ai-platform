import request from '@/utils/request'
import type { ChatDTO, ChatVO, ConversationListVO, ConversationVO, ModelInfo, PromptVO, PromptCreateDTO } from '@/types/ai'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** AI 生成摘要 */
export function generateSummary(content: string, maxLength?: number) {
  return request.post<ApiResult<string>>('/ai/generate/summary', {
    content,
    maxLength: maxLength || 200,
  })
}

/** AI 生成标签 */
export function generateTags(title: string, content: string, count?: number) {
  return request.post<ApiResult<string[]>>('/ai/generate/tags', {
    title,
    content,
    count: count || 5,
  })
}

/** AI 生成标题 */
export function generateTitles(content: string, count?: number) {
  return request.post<ApiResult<string[]>>('/ai/generate/titles', {
    content,
    count: count || 3,
  })
}

/** AI 对话（同步） */
export function chat(data: ChatDTO) {
  return request.post<ApiResult<ChatVO>>('/ai/conversations/chat', data)
}

/**
 * AI 流式对话（SSE）
 * 返回 AbortController，可通过 abort() 取消流式请求
 */
export async function chatStream(
  data: ChatDTO,
  onChunk: (text: string) => void,
  onDone?: () => void,
  onError?: (error: Error) => void
): Promise<AbortController> {
  const controller = new AbortController()
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const authStore = (await import('@/stores/auth')).useAuthStore()

  try {
    const response = await fetch(`${baseURL}/ai/conversations/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': authStore.token || '',
      },
      body: JSON.stringify(data),
      signal: controller.signal,
    })

    if (!response.ok) {
      throw new Error(`HTTP error: ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('ReadableStream not supported')
    }

    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 按双换行分割事件（SSE 标准）
      const events = buffer.split('\n\n')
      buffer = events.pop() || '' // 保留未完成的事件

      for (const event of events) {
        // 解析单个事件中的所有 data 行
        const dataLines = event.split('\n')
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5))

        if (dataLines.length > 0) {
          // 多行 data 用换行连接
          const text = dataLines.join('\n')
          if (text) {
            onChunk(text)
          }
        }
      }
    }

    // 处理 buffer 中剩余数据
    if (buffer.trim()) {
      const dataLines = buffer.split('\n')
        .filter(line => line.startsWith('data:'))
        .map(line => line.slice(5))

      if (dataLines.length > 0) {
        const text = dataLines.join('\n')
        if (text) {
          onChunk(text)
        }
      }
    }

    onDone?.()
  } catch (error: any) {
    if (error.name === 'AbortError') {
      onDone?.()
    } else {
      onError?.(error)
    }
  }

  return controller
}

/** 对话列表 */
export function getConversations(params: PageQuery) {
  return request.get<ApiResult<PageResult<ConversationListVO>>>('/ai/conversations', { params })
}

/** 对话详情 */
export function getConversationDetail(conversationId: number) {
  return request.get<ApiResult<ConversationVO>>(`/ai/conversations/${conversationId}`)
}

/** 删除对话 */
export function deleteConversation(conversationId: number) {
  return request.delete<ApiResult<void>>(`/ai/conversations/${conversationId}`)
}

/** 获取可用模型 */
export function getAvailableModels() {
  return request.get<ApiResult<ModelInfo[]>>('/ai/models')
}

/** 获取 Prompt 模板列表 */
export function getPrompts(params: PageQuery) {
  return request.get<ApiResult<PageResult<PromptVO>>>('/ai/prompts', { params })
}

/** 按分类获取 Prompt 模板 */
export function getPromptsByCategory(category: string) {
  return request.get<ApiResult<PromptVO[]>>(`/ai/prompts/category/${category}`)
}

/** 获取 Prompt 分类统计 */
export function getPromptCategories() {
  return request.get<ApiResult<Record<string, number>>>('/ai/prompts/categories')
}

/** 创建 Prompt 模板 */
export function createPrompt(data: PromptCreateDTO) {
  return request.post<ApiResult<PromptVO>>('/ai/prompts', data)
}

/** 更新 Prompt 模板 */
export function updatePrompt(id: number, data: PromptCreateDTO) {
  return request.put<ApiResult<PromptVO>>(`/ai/prompts/${id}`, data)
}

/** 删除 Prompt 模板 */
export function deletePrompt(id: number) {
  return request.delete<ApiResult<void>>(`/ai/prompts/${id}`)
}

/** 重命名对话 */
export function renameConversation(conversationId: number, title: string) {
  return request.put<ApiResult<void>>(`/ai/conversations/${conversationId}/title`, null, { params: { title } })
}
