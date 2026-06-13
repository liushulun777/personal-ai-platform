<template>
  <div class="ai-chat-page">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <NButton type="primary" size="small" block @click="handleNewChat">
          新建对话
        </NButton>
      </div>

      <div class="conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          :class="['conversation-item', { active: String(currentConversationId) === String(conv.id) }]"
          @click="handleSelectConversation(conv.id)"
        >
          <div class="conv-title">{{ conv.title }}</div>
          <div class="conv-meta">
            <span class="conv-model">{{ conv.model }}</span>
            <NButton
              quaternary
              size="tiny"
              type="error"
              @click.stop="handleDeleteConversation(conv.id)"
            >
              删除
            </NButton>
          </div>
        </div>

        <div v-if="conversations.length === 0" class="empty-list">
          <NEmpty description="暂无对话" />
        </div>
      </div>
    </div>

    <div class="chat-main">
      <AiChatPanel ref="chatPanelRef" @conversation-created="handleConversationCreated" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NButton, NEmpty, useDialog, useMessage } from 'naive-ui'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'
import { getConversations, deleteConversation } from '@/api/ai'
import type { ConversationListVO } from '@/types/ai'

const dialog = useDialog()
const message = useMessage()
const conversations = ref<ConversationListVO[]>([])
const currentConversationId = ref<string | number | undefined>()
const chatPanelRef = ref()

async function loadConversations() {
  try {
    const res = await getConversations({ current: 1, size: 100 })
    conversations.value = (res as any).data?.data?.records || []
  } catch {
    conversations.value = []
  }
}

function handleNewChat() {
  currentConversationId.value = undefined
  if (chatPanelRef.value) {
    chatPanelRef.value.handleClear()
  }
}

async function handleConversationCreated(conversationId: string) {
  currentConversationId.value = conversationId
  await loadConversations()
}

function handleSelectConversation(id: number) {
  currentConversationId.value = id
  chatPanelRef.value?.loadConversation(id)
}

async function handleDeleteConversation(id: number) {
  dialog.warning({
    title: '删除对话',
    content: '确定要删除这个对话吗？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteConversation(id)
        message.success('删除成功')
        if (String(currentConversationId.value) === String(id)) {
          currentConversationId.value = undefined
        }
        await loadConversations()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

onMounted(() => {
  loadConversations()
})
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  height: calc(100vh - 130px);
  gap: 16px;
}

.chat-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-card);
}

.sidebar-header {
  padding: 12px;
  border-bottom: 1px solid var(--border-color);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.2s;
}

.conversation-item:hover {
  background: var(--hover-bg);
}

.conversation-item.active {
  background: var(--accent-soft);
}

.conv-title {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conv-model {
  font-size: 11px;
  color: #999;
}

.empty-list {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.chat-main {
  flex: 1;
  min-width: 0;
}
</style>
