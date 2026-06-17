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
  getProjectPage,
  createProject,
  updateProject,
  deleteProject,
  aiDecomposeTasks,
  executeProjectTasks
} from '@/api/project'
import type { ProjectVO, ProjectCreateParams, ProjectUpdateParams } from '@/api/project'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const showAiModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const currentProjectId = ref<number | null>(null)
const aiContent = ref('')
const aiLoading = ref(false)
const executeLoading = ref(false)

const queryParams = ref({
  name: '',
  status: null as number | null,
  priority: null as number | null
})

const projects = ref<ProjectVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const formData = ref<ProjectCreateParams & { id?: number }>({
  name: '',
  description: '',
  priority: 1,
  ownerId: undefined,
  startDate: undefined,
  endDate: undefined
})

const formRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

const statusOptions = [
  { label: '规划中', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已归档', value: 3 }
]

const priorityOptions = [
  { label: '低', value: 0 },
  { label: '中', value: 1 },
  { label: '高', value: 2 },
  { label: '紧急', value: 3 }
]

const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '规划中', type: 'info' },
  1: { label: '进行中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已归档', type: 'info' }
}

const priorityMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '低', type: 'success' },
  1: { label: '中', type: 'info' },
  2: { label: '高', type: 'warning' },
  3: { label: '紧急', type: 'error' }
}

const columns: DataTableColumns<ProjectVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return h('span', {}, { default: () => (pagination.value.page - 1) * pagination.value.pageSize + index + 1 })
    }
  },
  { title: '项目名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
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
  { title: '开始日期', key: 'startDate', width: 120 },
  { title: '结束日期', key: 'endDate', width: 120 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 350,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          // AI 拆分任务按钮（规划中状态显示）
          row.status === 0 ? h(NButton, {
            size: 'small',
            type: 'warning',
            onClick: () => handleAiDecompose(row)
          }, { default: () => 'AI拆分任务' }) : null,
          // 一键执行按钮（进行中状态显示）
          row.status === 1 ? h(NButton, {
            size: 'small',
            type: 'success',
            loading: executeLoading.value,
            onClick: () => handleExecuteProject(row.id)
          }, { default: () => '一键执行' }) : null,
          h(NButton, {
            size: 'small',
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该项目？'
          })
        ].filter(Boolean)
      })
    }
  }
]

async function loadProjects() {
  loading.value = true
  try {
    const { data } = await getProjectPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      name: queryParams.value.name || undefined,
      status: queryParams.value.status ?? undefined,
      priority: queryParams.value.priority ?? undefined
    })
    projects.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadProjects()
}

function handleReset() {
  queryParams.value = { name: '', status: null, priority: null }
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  formData.value = { name: '', description: '', priority: 1, ownerId: undefined, startDate: undefined, endDate: undefined }
  showModal.value = true
}

function handleEdit(row: ProjectVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    status: row.status,
    priority: row.priority,
    ownerId: row.ownerId,
    startDate: row.startDate,
    endDate: row.endDate
  }
  showModal.value = true
}

// AI 拆分任务
function handleAiDecompose(row: ProjectVO) {
  currentProjectId.value = row.id
  aiContent.value = row.description || ''
  showAiModal.value = true
}

async function handleAiSubmit() {
  if (!currentProjectId.value) return
  aiLoading.value = true
  try {
    const { data } = await aiDecomposeTasks(currentProjectId.value, aiContent.value)
    message.success(`AI 拆分完成，创建了 ${data.data.length} 个任务`)
    showAiModal.value = false
    loadProjects()
  } catch (error) {
    message.error('AI 拆分任务失败')
  } finally {
    aiLoading.value = false
  }
}

// 一键执行项目任务
async function handleExecuteProject(projectId: number) {
  executeLoading.value = true
  try {
    const { data } = await executeProjectTasks(projectId)
    message.success(`已触发 ${data.data} 个任务执行`)
    loadProjects()
  } catch (error) {
    message.error('执行项目任务失败')
  } finally {
    executeLoading.value = false
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    if (isEdit.value) {
      await updateProject(formData.value as ProjectUpdateParams)
      message.success('更新成功')
    } else {
      await createProject(formData.value)
      message.success('创建成功')
    }
    showModal.value = false
    loadProjects()
  } catch (error) {
    // validation or api error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteProject(id)
    message.success('删除成功')
    loadProjects()
  } catch (error) {
    message.error('删除失败')
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadProjects()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadProjects()
}

onMounted(() => {
  loadProjects()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">项目管理</h2>
      <NButton type="primary" size="small" @click="handleAdd">
        新建项目
      </NButton>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.name"
        placeholder="项目名称"
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

    <!-- 项目列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="projects"
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
      :title="isEdit ? '编辑项目' : '新建项目'"
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
        <NFormItem label="项目名称" path="name">
          <NInput v-model:value="formData.name" placeholder="请输入项目名称" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" placeholder="请输入项目描述" />
        </NFormItem>
        <NFormItem label="优先级" path="priority">
          <NSelect v-model:value="formData.priority" :options="priorityOptions" />
        </NFormItem>
        <NFormItem v-if="isEdit" label="状态" path="status">
          <NSelect v-model:value="formData.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="开始日期" path="startDate">
          <NDatePicker v-model:formatted-value="formData.startDate" type="date" value-format="yyyy-MM-dd" clearable />
        </NFormItem>
        <NFormItem label="结束日期" path="endDate">
          <NDatePicker v-model:formatted-value="formData.endDate" type="date" value-format="yyyy-MM-dd" clearable />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- AI 拆分任务弹窗 -->
    <NModal
      v-model:show="showAiModal"
      title="AI 拆分任务"
      preset="card"
      style="width: 600px"
    >
      <div class="mb-4">
        <p class="text-sm text-gray-500 mb-2">
          AI 将根据项目描述自动拆分任务。您可以修改需求描述后再执行拆分。
        </p>
        <NInput
          v-model:value="aiContent"
          type="textarea"
          placeholder="请输入项目需求描述，AI 将自动拆分为多个子任务"
          :rows="8"
        />
      </div>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showAiModal = false">取消</NButton>
          <NButton type="primary" :loading="aiLoading" @click="handleAiSubmit">
            开始拆分
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
