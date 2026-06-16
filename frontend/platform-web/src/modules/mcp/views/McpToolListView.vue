<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">MCP 工具管理</h2>
      <NSpace>
        <NButton size="small" @click="handleSync" :loading="syncing">
          同步工具
        </NButton>
        <NButton type="primary" size="small" @click="handleAdd">
          添加工具
        </NButton>
      </NSpace>
    </div>

    <!-- 服务筛选 -->
    <div class="mb-4">
      <NSelect
        v-model:value="filterServerId"
        :options="serverOptions"
        placeholder="筛选服务"
        clearable
        style="width: 200px"
        @update:value="handleFilterChange"
      />
    </div>

    <NCard>
      <NDataTable
        :columns="columns"
        :data="tools"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: McpToolVO) => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </NCard>

    <!-- 添加工具弹窗 -->
    <NModal
      v-model:show="showModal"
      preset="dialog"
      title="添加 MCP 工具"
      positive-text="添加"
      negative-text="取消"
      :loading="submitting"
      @positive-click="handleSubmit"
      style="width: 600px"
    >
      <NForm ref="formRef" :model="formData" label-placement="left" label-width="100">
        <NFormItem label="所属服务" path="serverId" :rule="{ required: true, message: '请选择服务' }">
          <NSelect
            v-model:value="formData.serverId"
            :options="serverOptions"
            placeholder="请选择服务"
          />
        </NFormItem>
        <NFormItem label="工具名称" path="name" :rule="{ required: true, message: '请输入工具名称' }">
          <NInput v-model:value="formData.name" placeholder="请输入工具名称" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" :rows="2" placeholder="请输入工具描述" />
        </NFormItem>
        <NFormItem label="输入 Schema" path="inputSchema">
          <NInput v-model:value="formData.inputSchema" type="textarea" :rows="4" placeholder='JSON Schema，如 {"type":"object","properties":{}}' />
        </NFormItem>
      </NForm>
    </NModal>

    <!-- 调用测试弹窗 -->
    <NModal
      v-model:show="showInvokeModal"
      preset="dialog"
      title="调用测试"
      positive-text="调用"
      negative-text="关闭"
      :loading="invoking"
      @positive-click="handleInvoke"
      style="width: 600px"
    >
      <div v-if="invokingTool">
        <p class="mb-2 text-sm" style="color: var(--text-secondary)">
          工具: <strong>{{ invokingTool.name }}</strong>
        </p>
        <p class="mb-4 text-xs" style="color: var(--text-muted)">
          {{ invokingTool.description }}
        </p>
        <NFormItem label="参数 (JSON)">
          <NInput
            v-model:value="invokeArgs"
            type="textarea"
            :rows="4"
            placeholder='{"key": "value"}'
          />
        </NFormItem>
        <div v-if="invokeResult" class="mt-4">
          <p class="text-sm font-medium mb-2">调用结果:</p>
          <NInput
            :value="JSON.stringify(invokeResult, null, 2)"
            type="textarea"
            :rows="6"
            readonly
          />
        </div>
      </div>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import {
  NCard,
  NButton,
  NDataTable,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NSpace,
  NTag,
  useMessage,
  useDialog,
} from 'naive-ui'
import type { DataTableColumns, FormInst, PaginationProps } from 'naive-ui'
import {
  getMcpToolPage,
  createMcpTool,
  deleteMcpTool,
  invokeMcpTool,
  syncMcpTools,
  getMcpServerPage,
} from '@/api/mcp'
import type { McpToolVO, McpToolCreateParams } from '@/api/mcp'

const message = useMessage()
const dialog = useDialog()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const submitting = ref(false)
const syncing = ref(false)
const invoking = ref(false)
const showModal = ref(false)
const showInvokeModal = ref(false)
const tools = ref<McpToolVO[]>([])
const filterServerId = ref<number | null>(null)
const serverOptions = ref<{ label: string; value: number }[]>([])

const formData = reactive<McpToolCreateParams>({
  serverId: 0,
  name: '',
  description: '',
  inputSchema: '',
})

const invokingTool = ref<McpToolVO | null>(null)
const invokeArgs = ref('{}')
const invokeResult = ref<Record<string, unknown> | null>(null)

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const columns: DataTableColumns<McpToolVO> = [
  { title: '工具名称', key: 'name', width: 150 },
  { title: '服务', key: 'serverName', width: 120 },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, {
        size: 'small',
        type: row.status === 1 ? 'success' : 'warning',
      }, { default: () => row.status === 1 ? '启用' : '禁用' })
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'tiny',
            quaternary: true,
            type: 'info',
            onClick: () => handleInvokeTest(row),
          }, { default: () => '调用' }),
          h(NButton, {
            size: 'tiny',
            quaternary: true,
            type: 'error',
            onClick: () => handleDelete(row),
          }, { default: () => '删除' }),
        ],
      })
    },
  },
]

async function loadServers() {
  try {
    const res = await getMcpServerPage({ current: 1, size: 100 })
    const records = res.data.data?.records || []
    serverOptions.value = records.map(s => ({ label: s.name, value: s.id }))
  } catch {
    serverOptions.value = []
  }
}

async function loadTools() {
  loading.value = true
  try {
    const res = await getMcpToolPage({
      current: pagination.page || 1,
      size: pagination.pageSize || 10,
      serverId: filterServerId.value || undefined,
    })
    const pageData = res.data.data
    tools.value = pageData?.records || []
    pagination.itemCount = pageData?.total
  } catch {
    tools.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  formData.serverId = filterServerId.value || 0
  formData.name = ''
  formData.description = ''
  formData.inputSchema = ''
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return false
  }

  submitting.value = true
  try {
    await createMcpTool(formData)
    message.success('添加成功')
    showModal.value = false
    await loadTools()
    return true
  } catch {
    message.error('添加失败')
    return false
  } finally {
    submitting.value = false
  }
}

function handleInvokeTest(row: McpToolVO) {
  invokingTool.value = row
  invokeArgs.value = '{}'
  invokeResult.value = null
  showInvokeModal.value = true
}

async function handleInvoke() {
  if (!invokingTool.value) return false

  invoking.value = true
  try {
    let args = {}
    try {
      args = JSON.parse(invokeArgs.value)
    } catch {
      message.error('参数格式错误，请输入有效的 JSON')
      return false
    }
    const res = await invokeMcpTool({
      toolId: invokingTool.value.id,
      arguments: args,
    })
    invokeResult.value = res.data.data
    return true
  } catch {
    message.error('调用失败')
    return false
  } finally {
    invoking.value = false
  }
}

async function handleSync() {
  if (!filterServerId.value) {
    message.warning('请先选择一个服务')
    return
  }
  syncing.value = true
  try {
    await syncMcpTools(filterServerId.value)
    message.success('同步完成')
    await loadTools()
  } catch {
    message.error('同步失败')
  } finally {
    syncing.value = false
  }
}

function handleDelete(row: McpToolVO) {
  dialog.warning({
    title: '删除工具',
    content: `确定要删除工具 "${row.name}" 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMcpTool(row.id)
        message.success('删除成功')
        await loadTools()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

function handleFilterChange() {
  pagination.page = 1
  loadTools()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadTools()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadTools()
}

onMounted(() => {
  loadServers()
  loadTools()
})
</script>
