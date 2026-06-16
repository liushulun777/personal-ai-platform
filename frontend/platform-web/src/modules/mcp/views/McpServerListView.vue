<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">MCP 服务管理</h2>
      <NButton type="primary" size="small" @click="handleAdd">
        注册服务
      </NButton>
    </div>

    <NCard>
      <NDataTable
        :columns="columns"
        :data="servers"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: McpServerVO) => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </NCard>

    <!-- 注册/编辑服务弹窗 -->
    <NModal
      v-model:show="showModal"
      preset="dialog"
      :title="isEdit ? '编辑 MCP 服务' : '注册 MCP 服务'"
      :positive-text="isEdit ? '保存' : '注册'"
      negative-text="取消"
      :loading="submitting"
      @positive-click="handleSubmit"
      style="width: 600px"
    >
      <NForm ref="formRef" :model="formData" label-placement="left" label-width="100">
        <NFormItem label="服务名称" path="name" :rule="{ required: true, message: '请输入服务名称' }">
          <NInput v-model:value="formData.name" placeholder="请输入服务名称" />
        </NFormItem>
        <NFormItem label="传输类型" path="transportType" :rule="{ required: true, message: '请选择传输类型' }">
          <NSelect
            v-model:value="formData.transportType"
            :options="transportOptions"
            placeholder="请选择传输类型"
          />
        </NFormItem>
        <NFormItem v-if="formData.transportType !== 'stdio'" label="端点地址" path="endpoint">
          <NInput v-model:value="formData.endpoint" placeholder="SSE/HTTP 端点地址" />
        </NFormItem>
        <NFormItem v-if="formData.transportType === 'stdio'" label="启动命令" path="command">
          <NInput v-model:value="formData.command" placeholder="stdio 启动命令" />
        </NFormItem>
        <NFormItem v-if="formData.transportType === 'stdio'" label="启动参数" path="args">
          <NInput v-model:value="formData.args" placeholder='JSON 数组，如 ["--verbose"]' />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" :rows="3" placeholder="请输入服务描述" />
        </NFormItem>
      </NForm>
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
  NTag,
  NSpace,
  useMessage,
  useDialog,
} from 'naive-ui'
import type { DataTableColumns, FormInst, PaginationProps } from 'naive-ui'
import {
  getMcpServerPage,
  createMcpServer,
  updateMcpServer,
  deleteMcpServer,
  enableMcpServer,
  disableMcpServer,
} from '@/api/mcp'
import type { McpServerVO, McpServerCreateParams } from '@/api/mcp'

const message = useMessage()
const dialog = useDialog()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const servers = ref<McpServerVO[]>([])

const formData = reactive<McpServerCreateParams>({
  name: '',
  description: '',
  transportType: 'stdio',
  endpoint: '',
  command: '',
  args: '',
})

const transportOptions = [
  { label: 'stdio', value: 'stdio' },
  { label: 'SSE', value: 'sse' },
  { label: 'Streamable HTTP', value: 'streamable_http' },
]

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const columns: DataTableColumns<McpServerVO> = [
  { title: '名称', key: 'name', width: 150 },
  {
    title: '传输类型',
    key: 'transportType',
    width: 120,
    render(row) {
      return h(NTag, { size: 'small', type: 'info' }, { default: () => row.transportType })
    },
  },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '工具数', key: 'toolCount', width: 80 },
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
    width: 250,
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'tiny',
            quaternary: true,
            onClick: () => handleEdit(row),
          }, { default: () => '编辑' }),
          h(NButton, {
            size: 'tiny',
            quaternary: true,
            type: row.status === 1 ? 'warning' : 'success',
            onClick: () => handleToggleStatus(row),
          }, { default: () => row.status === 1 ? '禁用' : '启用' }),
          h(NButton, {
            size: 'tiny',
            quaternary: true,
            type: 'info',
            onClick: () => handleViewTools(row),
          }, { default: () => '工具' }),
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
  loading.value = true
  try {
    const res = await getMcpServerPage({
      current: pagination.page || 1,
      size: pagination.pageSize || 10,
    })
    const pageData = res.data.data
    servers.value = pageData?.records || []
    pagination.itemCount = pageData?.total
  } catch {
    servers.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

function handleEdit(row: McpServerVO) {
  isEdit.value = true
  editingId.value = row.id
  formData.name = row.name
  formData.description = row.description || ''
  formData.transportType = row.transportType
  formData.endpoint = row.endpoint || ''
  formData.command = row.command || ''
  formData.args = ''
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
    if (isEdit.value && editingId.value) {
      await updateMcpServer(editingId.value, formData)
      message.success('更新成功')
    } else {
      await createMcpServer(formData)
      message.success('注册成功')
    }
    showModal.value = false
    resetForm()
    await loadServers()
    return true
  } catch {
    message.error(isEdit.value ? '更新失败' : '注册失败')
    return false
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: McpServerVO) {
  try {
    if (row.status === 1) {
      await disableMcpServer(row.id)
      message.success('已禁用')
    } else {
      await enableMcpServer(row.id)
      message.success('已启用')
    }
    await loadServers()
  } catch {
    message.error('操作失败')
  }
}

function handleViewTools(row: McpServerVO) {
  // Navigate to tools page with server filter
  window.location.hash = `#/mcp/tools?serverId=${row.id}`
}

function handleDelete(row: McpServerVO) {
  dialog.warning({
    title: '删除服务',
    content: `确定要删除服务 "${row.name}" 吗？关联的工具和资源将同时删除。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMcpServer(row.id)
        message.success('删除成功')
        await loadServers()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

function resetForm() {
  formData.name = ''
  formData.description = ''
  formData.transportType = 'stdio'
  formData.endpoint = ''
  formData.command = ''
  formData.args = ''
}

function handlePageChange(page: number) {
  pagination.page = page
  loadServers()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadServers()
}

onMounted(() => {
  loadServers()
})
</script>
