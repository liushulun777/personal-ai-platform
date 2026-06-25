<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { NDataTable, NTag, NButton, NTimeline, NTimelineItem, NModal, NEmpty, NTooltip, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

interface TaskExecution {
  id: number
  taskId: number
  taskTitle: string
  projectId: number
  projectName: string
  status: number
  startTime: string
  endTime: string
  duration: number
  errorMessage: string
  resultSummary: string
  steps: ExecutionStep[]
}

interface ExecutionStep {
  id: number
  stepName: string
  stepOrder: number
  status: number
  startTime: string
  endTime: string
  duration: number
  detail: string
  errorMessage: string
}

const message = useMessage()
const loading = ref(false)
const executions = ref<TaskExecution[]>([])
const showDetailModal = ref(false)
const currentExecution = ref<TaskExecution | null>(null)
const cancellingExecutionId = ref<number | null>(null)

const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '待执行', type: 'info' },
  1: { label: '执行中', type: 'warning' },
  2: { label: '成功', type: 'success' },
  3: { label: '失败', type: 'error' },
  4: { label: '超时', type: 'error' },
  5: { label: '已取消', type: 'default' as any }
}

const columns: DataTableColumns<TaskExecution> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '任务', key: 'taskTitle', width: 200, ellipsis: { tooltip: true } },
  { title: '项目', key: 'projectName', width: 150 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const s = statusMap[row.status]
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '未知' })
    }
  },
  {
    title: '耗时',
    key: 'duration',
    width: 100,
    render(row) {
      if (!row.duration) return h('span', {}, { default: () => '-' })
      const time = row.duration > 1000 ? (row.duration / 1000).toFixed(1) + 's' : row.duration + 'ms'
      return h('span', {}, { default: () => time })
    }
  },
  { title: '开始时间', key: 'startTime', width: 170 },
  {
    title: '结果',
    key: 'resultSummary',
    width: 200,
    ellipsis: { tooltip: true },
    render(row) {
      if (row.errorMessage) {
        return h(NTooltip, { trigger: 'hover' }, {
          trigger: () => h('span', { style: 'color: #F56C6C' }, { default: () => row.errorMessage?.substring(0, 30) + '...' }),
          default: () => row.errorMessage
        })
      }
      return h('span', {}, { default: () => row.resultSummary || '-' })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      const actions = [
        h(NButton, {
          size: 'small',
          quaternary: true,
          type: 'info',
          onClick: () => showDetail(row)
        }, { default: () => '详情' })
      ]

      // 执行中的任务可以取消
      if (row.status === 1) {
        actions.push(
          h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'warning',
            loading: cancellingExecutionId.value === row.taskId,
            onClick: () => handleCancelTask(row.taskId)
          }, { default: () => '取消' })
        )
      }

      return h(NButton, { size: 'small' }, { default: () => actions })
    }
  }
]

async function loadExecutions() {
  loading.value = true
  try {
    const { data } = await request.get<ApiResult<TaskExecution[]>>('/agent/executions')
    executions.value = data.data || []
  } catch {
    executions.value = []
  } finally {
    loading.value = false
  }
}

function showDetail(execution: TaskExecution) {
  currentExecution.value = execution
  showDetailModal.value = true
}

// 取消任务执行
async function handleCancelTask(taskId: number) {
  cancellingExecutionId.value = taskId
  try {
    await request.post(`/agent/tasks/${taskId}/cancel`)
    message.success('任务已取消')
    await loadExecutions()
  } catch {
    message.error('取消失败')
  } finally {
    cancellingExecutionId.value = null
  }
}

function getTimelineType(status: number) {
  const typeMap: Record<number, 'success' | 'warning' | 'error' | 'info'> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'error',
    4: 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化持续时间
function formatDuration(ms: number | null | undefined): string {
  if (!ms) return '-'
  if (ms < 1000) return ms + 'ms'
  if (ms < 60000) return (ms / 1000).toFixed(1) + 's'
  return (ms / 60000).toFixed(1) + 'min'
}

onMounted(() => {
  loadExecutions()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">Agent 执行记录</h2>
      <NButton size="small" @click="loadExecutions" :loading="loading">
        刷新
      </NButton>
    </div>

    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="executions"
        :loading="loading"
        :bordered="false"
        :single-line="false"
      />
    </div>

    <div v-if="!loading && executions.length === 0" class="mt-8">
      <NEmpty description="暂无执行记录" />
    </div>

    <!-- 执行详情弹窗 -->
    <NModal
      v-model:show="showDetailModal"
      title="执行详情"
      preset="card"
      style="width: 700px"
    >
      <div v-if="currentExecution">
        <!-- 基本信息 -->
        <div class="detail-header mb-4">
          <div class="detail-item">
            <span class="label">执行 ID：</span>
            <span>{{ currentExecution.id }}</span>
          </div>
          <div class="detail-item">
            <span class="label">任务：</span>
            <span>{{ currentExecution.taskTitle }}</span>
          </div>
          <div class="detail-item">
            <span class="label">项目：</span>
            <span>{{ currentExecution.projectName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">状态：</span>
            <NTag :type="statusMap[currentExecution.status]?.type" size="small">
              {{ statusMap[currentExecution.status]?.label }}
            </NTag>
          </div>
          <div class="detail-item">
            <span class="label">耗时：</span>
            <span>{{ formatDuration(currentExecution.duration) }}</span>
          </div>
          <div v-if="currentExecution.startTime" class="detail-item">
            <span class="label">开始时间：</span>
            <span>{{ currentExecution.startTime }}</span>
          </div>
          <div v-if="currentExecution.endTime" class="detail-item">
            <span class="label">结束时间：</span>
            <span>{{ currentExecution.endTime }}</span>
          </div>
          <div v-if="currentExecution.resultSummary" class="detail-item">
            <span class="label">结果：</span>
            <span>{{ currentExecution.resultSummary }}</span>
          </div>
          <div v-if="currentExecution.errorMessage" class="detail-item error">
            <span class="label">错误：</span>
            <span>{{ currentExecution.errorMessage }}</span>
          </div>
        </div>

        <!-- 执行步骤 -->
        <div class="steps-title mb-2 font-medium" style="color: var(--text-primary)">执行步骤</div>
        <div v-if="currentExecution.steps?.length" class="steps-container">
          <NTimeline>
            <NTimelineItem
              v-for="step in currentExecution.steps"
              :key="step.id"
              :type="getTimelineType(step.status)"
              :title="step.stepName"
            >
              <template #default>
                <div class="step-content">
                  <div v-if="step.detail" class="step-detail">{{ step.detail }}</div>
                  <div v-if="step.errorMessage" class="step-error">{{ step.errorMessage }}</div>
                  <div class="step-meta">
                    <span v-if="step.startTime">{{ step.startTime }}</span>
                    <span v-if="step.duration"> · {{ formatDuration(step.duration) }}</span>
                  </div>
                </div>
              </template>
            </NTimelineItem>
          </NTimeline>
        </div>
        <div v-else class="text-center py-4" style="color: var(--text-muted)">
          暂无执行步骤信息
        </div>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.detail-header {
  background: var(--bg-card);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item .label {
  color: var(--text-secondary);
  margin-right: 8px;
  min-width: 80px;
  flex-shrink: 0;
}

.detail-item.error {
  color: #F56C6C;
}

.steps-title {
  color: var(--text-primary);
}

.steps-container {
  max-height: 400px;
  overflow-y: auto;
}

.step-content {
  font-size: 13px;
}

.step-detail {
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.step-error {
  color: #F56C6C;
  margin-bottom: 4px;
}

.step-meta {
  color: var(--text-muted);
  font-size: 12px;
}
</style>
