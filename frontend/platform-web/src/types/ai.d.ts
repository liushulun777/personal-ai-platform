/** AI 对话请求 */
export interface ChatDTO {
  message: string
  conversationId?: number
  model?: string
}

/** AI 对话响应 */
export interface ChatVO {
  conversationId: number
  reply: string
  tokenCount: number
}

/** 对话列表项 */
export interface ConversationListVO {
  id: number
  title: string
  model: string
  lastMessage: string
  createTime: string
}

/** 对话详情 */
export interface ConversationVO {
  id: number
  title: string
  model: string
  messages: MessageVO[]
  createTime: string
}

/** 消息 */
export interface MessageVO {
  id: number
  role: 'system' | 'user' | 'assistant'
  content: string
  tokenCount: number
  createTime: string
}

/** 模型信息 */
export interface ModelInfo {
  name: string
  model: string
  isDefault: string
}

/** Prompt 模板 */
export interface PromptVO {
  id: number
  name: string
  description: string
  promptText: string
  category: string
  status: number
  createTime: string
}

/** Prompt 模板创建 */
export interface PromptCreateDTO {
  name: string
  description?: string
  promptText: string
  category?: string
}
