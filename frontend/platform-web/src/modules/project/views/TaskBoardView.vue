<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NButton, NTag, NEmpty, NScrollbar, NSpin, useMessage } from 'naive-ui'
import { getTaskPage, startTask, completeTask, submitReview, approveTask, rejectTask, blockTask, unblockTask } from '@/api/project'
import type { TaskVO } from '@/api/project'

const message = useMessage()
const loading = ref(false)
const tasks = ref<TaskVO[]>([])

// 状态定义
const statusColumns = [
  { key: 0, label: 'BACKLOG', color: '#909399' },
  { key: 1, label: 'READY', color: '#409EFF' },
  { key: 2, label: 'DOING', color: '#E6A23C' },
  { key: 3, label: 'REVIEW', color: '#8B5CF6' },
  { key: 4, label: 'DONE', color: '#67C23A' },
  { key: 5, label: 'BLOCKED', color: '#F56C6C' }
]

// 按状态分组的任务
const tasksByStatus = computed(() => {
  const grouped: Record<number, TaskVO[]> = {}
  statusColumns.forEach(col => {
    grouped[col.key] = tasks.value.filter(t => t.status === col.key)
  })
  return grouped
})

// 优先级标签颜色
const priorityColors: Record<number, string> = {
  0: 'success',
  1: 'info',
  2: 'warning',
  3: 'error'
}

const priorityLabels: Record<number, string> = {
  0: 'LOW',
  1: 'MEDIUM',
  2: 'HIGH',
  3: 'URGENT'
}

async function loadTasks() {
  loading.value = true
  try {
    const { data } = await getTaskPage({ current: 1, size: 1000 })
    tasks.value = data.data?.records || []
  } catch {
    tasks.value = []
  } finally {
    loading.value = false
  }
}

// 获取可用的状态流转操作
function getAvailableActions(task: TaskVO) {
  const actions: { label: string; type: string; handler: () => Promise<void> }[] = []

  switch (task.status) {
    case 0: // BACKLOG
    case 1: // READY
      actions.push({
        label: '开始',
        type: 'primary',
        handler: async () => {
          await startTask(task.id)
          message.success('任务已开始')
          await loadTasks()
        }
      })
      break
    case 2: // DOING
      actions.push(
        {
          label: '完成',
          type: 'success',
          handler: async () => {
            await completeTask(task.id)
            message.success('任务已完成')
            await loadTasks()
          }
        },
        {
          label: '提交审核',
          type: 'info',
          handler: async () => {
            await submitReview(task.id)
            message.success('已提交审核')
            await loadTasks()
          }
        },
        {
          label: '阻塞',
          type: 'warning',
          handler: async () => {
            await blockTask(task.id, '手动阻塞')
            message.success('任务已阻塞')
            await loadTasks()
          }
        }
      )
      break
    case 3: // REVIEW
      actions.push(
        {
          label: '通过',
          type: 'success',
          handler: async () => {
            await approveTask(task.id)
            message.success('审核通过')
            await loadTasks()
          }
        },
        {
          label: '拒绝',
          type: 'error',
          handler: async () => {
            await rejectTask(task.id, '审核拒绝')
            message.success('已拒绝')
            await loadTasks()
          }
        }
      )
      break
    case 5: // BLOCKED
      actions.push({
        label: '解除阻塞',
        type: 'primary',
        handler: async () => {
          await unblockTask(task.id)
          message.success('已解除阻塞')
          await loadTasks()
        }
      })
      break
  }

  return actions
}

onMounted(() => {
  loadTasks()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">任务看板</h2>
      <NButton size="small" @click="loadTasks" :loading="loading">
        刷新
      </NButton>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <NSpin size="large" />
    </div>

    <div v-else class="kanban-board">
      <div
        v-for="col in statusColumns"
        :key="col.key"
        class="kanban-column"
      >
        <div class="column-header" :style="{ borderTopColor: col.color }">
          <span class="column-title">{{ col.label }}</span>
          <NTag size="small" :bordered="false">
            {{ tasksByStatus[col.key]?.length || 0 }}
          </NTag>
        </div>

        <NScrollbar class="column-content">
          <div v-if="!tasksByStatus[col.key]?.length" class="empty-column">
            <NEmpty description="暂无任务" size="small" />
          </div>

          <div
            v-for="task in tasksByStatus[col.key]"
            :key="task.id"
            class="task-card"
          >
            <div class="task-header">
              <span class="task-title">{{ task.title }}</span>
              <NTag
                size="tiny"
                :type="priorityColors[task.priority] as any"
              >
                {{ priorityLabels[task.priority] }}
              </NTag>
            </div>

            <div v-if="task.description" class="task-desc">
              {{ task.description }}
            </div>

            <div class="task-meta">
              <span v-if="task.dueDate" class="task-date">
                📅 {{ task.dueDate }}
              </span>
            </div>

            <div class="task-actions">
              <NButton
                v-for="action in getAvailableActions(task)"
                :key="action.label"
                size="tiny"
                :type="action.type as any"
                @click="action.handler()"
              >
                {{ action.label }}
              </NButton>
            </div>
          </div>
        </NScrollbar>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kanban-board {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 16px;
  min-height: calc(100vh - 200px);
}

.kanban-column {
  flex: 1;
  min-width: 250px;
  max-width: 300px;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.column-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 3px solid;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
}

.column-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.column-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

.empty-column {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
}

.task-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.task-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.task-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.task-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
  word-break: break-word;
}

.task-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.task-actions {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
