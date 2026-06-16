<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import {
  NDataTable,
  NButton,
  NSpace,
  NInput,
  NSelect,
  NModal,
  NForm,
  NFormItem,
  NPopconfirm,
  NTag,
  NDatePicker,
  useMessage
} from 'naive-ui'
import type { DataTableColumns, FormInst } from 'naive-ui'
import {
  getTaskPage,
  createTask,
  updateTask,
  deleteTask
} from '@/api/project'
import type { TaskVO, TaskCreateParams, TaskUpdateParams } from '@/api/project'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const queryParams = ref({
  title: '',
  status: null as number | null,
  priority: null as number | null
})

const tasks = ref<TaskVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const formData = ref<TaskCreateParams & { id?: number }>({
  projectId: 0,
  title: '',
  description: '',
  priority: 1,
  assigneeId: undefined,
  dueDate: undefined
})

const formRules = {
  projectId: [{ required: true, message: '请输入项目ID', trigger: 'blur' }],
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

const statusOptions = [
  { label: '待办', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已关闭', value: 3 }
]

const priorityOptions = [
  { label: '低', value: 0 },
  { label: '中', value: 1 },
  { label: '高', value: 2 },
  { label: '紧急', value: 3 }
]

const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '待办', type: 'info' },
  1: { label: '进行中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已关闭', type: 'info' }
}

const priorityMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '低', type: 'success' },
  1: { label: '中', type: 'info' },
  2: { label: '高', type: 'warning' },
  3: { label: '紧急', type: 'error' }
}

const columns: DataTableColumns<TaskVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '项目ID', key: 'projectId', width: 80 },
  { title: '标题', key: 'title', width: 200, ellipsis: { tooltip: true } },
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
    title: '优先级',
    key: 'priority',
    width: 80,
    render(row) {
      const p = priorityMap[row.priority]
      return h(NTag, { type: p?.type, size: 'small' }, { default: () => p?.label || '未知' })
    }
  },
  { title: '截止日期', key: 'dueDate', width: 120 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, {
            size: 'small',
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该任务？'
          })
        ]
      })
    }
  }
]

async function loadTasks() {
  loading.value = true
  try {
    const { data } = await getTaskPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      title: queryParams.value.title || undefined,
      status: queryParams.value.status ?? undefined,
      priority: queryParams.value.priority ?? undefined
    })
    tasks.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadTasks()
}

function handleReset() {
  queryParams.value = { title: '', status: null, priority: null }
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  formData.value = { projectId: 0, title: '', description: '', priority: 1, assigneeId: undefined, dueDate: undefined }
  showModal.value = true
}

function handleEdit(row: TaskVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    projectId: row.projectId,
    title: row.title,
    description: row.description,
    status: row.status,
    priority: row.priority,
    assigneeId: row.assigneeId,
    dueDate: row.dueDate
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    if (isEdit.value) {
      await updateTask(formData.value as TaskUpdateParams)
      message.success('更新成功')
    } else {
      await createTask(formData.value)
      message.success('创建成功')
    }
    showModal.value = false
    loadTasks()
  } catch (error) {
    // validation or api error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteTask(id)
    message.success('删除成功')
    loadTasks()
  } catch (error) {
    message.error('删除失败')
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadTasks()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadTasks()
}

onMounted(() => {
  loadTasks()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">任务管理</h2>
      <NButton type="primary" size="small" @click="handleAdd">
        新建任务
      </NButton>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.title"
        placeholder="任务标题"
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
        class="w-32"
      />
      <NSelect
        v-model:value="queryParams.priority"
        :options="priorityOptions"
        placeholder="优先级"
        clearable
        size="small"
        class="w-32"
      />
      <NButton size="small" type="primary" @click="handleSearch">搜索</NButton>
      <NButton size="small" @click="handleReset">重置</NButton>
    </div>

    <!-- 任务列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="tasks"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :pagination="pagination"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- 新建/编辑弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="isEdit ? '编辑任务' : '新建任务'"
      preset="card"
      style="width: 500px"
    >
      <NForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80"
      >
        <NFormItem label="项目ID" path="projectId">
          <NInput v-model:value="formData.projectId" placeholder="请输入项目ID" />
        </NFormItem>
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入任务标题" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" placeholder="请输入任务描述" />
        </NFormItem>
        <NFormItem label="优先级" path="priority">
          <NSelect v-model:value="formData.priority" :options="priorityOptions" />
        </NFormItem>
        <NFormItem v-if="isEdit" label="状态" path="status">
          <NSelect v-model:value="formData.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="截止日期" path="dueDate">
          <NDatePicker v-model:formatted-value="formData.dueDate" type="date" value-format="yyyy-MM-dd" clearable />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
