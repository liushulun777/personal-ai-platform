<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import {
  NCard,
  NDataTable,
  NButton,
  NSpace,
  NInput,
  NSelect,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NPopconfirm,
  NTag,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getLogPage, getLogById, deleteLog } from '@/api/log'
import type { LogVO, LogQueryParams } from '@/api/log'

const message = useMessage()
const loading = ref(false)
const showDetail = ref(false)
const currentLog = ref<LogVO | null>(null)

const queryParams = ref({
  module: '',
  operation: '',
  status: null as number | null,
  username: ''
})

const logList = ref<LogVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const statusOptions = [
  { label: '成功', value: 1 },
  { label: '失败', value: 0 }
]

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '成功', type: 'success' },
  0: { label: '失败', type: 'error' }
}

const columns: DataTableColumns<LogVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '模块', key: 'module', width: 100 },
  { title: '操作', key: 'operation', width: 150, ellipsis: { tooltip: true } },
  { title: '用户名', key: 'username', width: 100 },
  { title: 'IP', key: 'ip', width: 130 },
  {
    title: '状态',
    key: 'status',
    width: 80,
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
      return row.duration ? `${row.duration}ms` : '-'
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleDetail(row) }, { default: () => '详情' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该日志？'
          })
        ]
      })
    }
  }
]

async function loadLogs() {
  loading.value = true
  try {
    const { data } = await getLogPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      module: queryParams.value.module || undefined,
      operation: queryParams.value.operation || undefined,
      status: queryParams.value.status ?? undefined,
      username: queryParams.value.username || undefined
    })
    logList.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadLogs()
}

function handleReset() {
  queryParams.value = { module: '', operation: '', status: null, username: '' }
  handleSearch()
}

async function handleDetail(row: LogVO) {
  try {
    const { data } = await getLogById(row.id)
    currentLog.value = data.data
    showDetail.value = true
  } catch (error) {
    message.error('获取日志详情失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deleteLog(id)
    message.success('删除成功')
    loadLogs()
  } catch (error) {
    message.error('删除失败')
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadLogs()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<template>
  <div>
    <h2 class="text-lg font-semibold mb-6" style="color: var(--text-primary)">操作日志</h2>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.module"
        placeholder="模块"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NInput
        v-model:value="queryParams.operation"
        placeholder="操作"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NInput
        v-model:value="queryParams.username"
        placeholder="用户名"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NSelect
        v-model:value="queryParams.status"
        :options="statusOptions"
        placeholder="状态"
        clearable
        size="small"
        class="w-28"
      />
      <NButton size="small" type="primary" @click="handleSearch">搜索</NButton>
      <NButton size="small" @click="handleReset">重置</NButton>
    </div>

    <!-- 日志列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="logList"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :pagination="pagination"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- 详情弹窗 -->
    <NModal
      v-model:show="showDetail"
      title="日志详情"
      preset="card"
      style="width: 700px"
    >
      <NDescriptions v-if="currentLog" bordered :column="2" label-placement="left">
        <NDescriptionsItem label="模块">{{ currentLog.module }}</NDescriptionsItem>
        <NDescriptionsItem label="操作">{{ currentLog.operation }}</NDescriptionsItem>
        <NDescriptionsItem label="方法">{{ currentLog.method }}</NDescriptionsItem>
        <NDescriptionsItem label="请求方法">{{ currentLog.requestMethod }}</NDescriptionsItem>
        <NDescriptionsItem label="请求URL" :span="2">{{ currentLog.requestUrl }}</NDescriptionsItem>
        <NDescriptionsItem label="IP">{{ currentLog.ip }}</NDescriptionsItem>
        <NDescriptionsItem label="用户名">{{ currentLog.username }}</NDescriptionsItem>
        <NDescriptionsItem label="状态">
          <NTag :type="statusMap[currentLog.status]?.type" size="small">
            {{ statusMap[currentLog.status]?.label || '未知' }}
          </NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="耗时">{{ currentLog.duration }}ms</NDescriptionsItem>
        <NDescriptionsItem label="创建时间" :span="2">{{ currentLog.createTime }}</NDescriptionsItem>
        <NDescriptionsItem label="请求参数" :span="2">
          <pre class="text-xs max-h-40 overflow-auto whitespace-pre-wrap">{{ currentLog.requestParams }}</pre>
        </NDescriptionsItem>
        <NDescriptionsItem v-if="currentLog.responseData" label="响应数据" :span="2">
          <pre class="text-xs max-h-40 overflow-auto whitespace-pre-wrap">{{ currentLog.responseData }}</pre>
        </NDescriptionsItem>
        <NDescriptionsItem v-if="currentLog.errorMsg" label="错误信息" :span="2">
          <span class="text-red-500">{{ currentLog.errorMsg }}</span>
        </NDescriptionsItem>
      </NDescriptions>
    </NModal>
  </div>
</template>
