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
  getBugPage,
  createBug,
  updateBug,
  deleteBug,
  getProjectPage
} from '@/api/project'
import type { BugVO, BugCreateParams, BugUpdateParams, ProjectVO } from '@/api/project'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const queryParams = ref({
  projectId: null as number | null,
  title: '',
  severity: null as number | null,
  status: null as number | null
})

const bugs = ref<BugVO[]>([])
const projects = ref<ProjectVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const formData = ref<BugCreateParams & { id?: number; status?: number }>({
  projectId: undefined,
  title: '',
  description: '',
  severity: 1,
  assigneeId: undefined,
  dueDate: undefined
})

const formRules = {
  title: [{ required: true, message: '请输入Bug标题', trigger: 'blur' }]
}

const severityOptions = [
  { label: '轻微', value: 0 },
  { label: '一般', value: 1 },
  { label: '严重', value: 2 },
  { label: '致命', value: 3 }
]

const statusOptions = [
  { label: '待确认', value: 0 },
  { label: '已确认', value: 1 },
  { label: '修复中', value: 2 },
  { label: '已修复', value: 3 },
  { label: '已关闭', value: 4 }
]

const severityMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '轻微', type: 'success' },
  1: { label: '一般', type: 'info' },
  2: { label: '严重', type: 'warning' },
  3: { label: '致命', type: 'error' }
}

const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '待确认', type: 'info' },
  1: { label: '已确认', type: 'warning' },
  2: { label: '修复中', type: 'warning' },
  3: { label: '已修复', type: 'success' },
  4: { label: '已关闭', type: 'info' }
}

// 项目选项
const projectOptions = ref<{ label: string; value: number }[]>([])

// 获取项目名称
function getProjectName(projectId: number): string {
  const project = projects.value.find(p => p.id === projectId)
  return project ? project.name : String(projectId)
}

const columns: DataTableColumns<BugVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return h('span', {}, { default: () => (pagination.value.page - 1) * pagination.value.pageSize + index + 1 })
    }
  },
  {
    title: '所属项目',
    key: 'projectId',
    width: 120,
    render(row) {
      return h('span', {}, { default: () => getProjectName(row.projectId) })
    }
  },
  { title: '标题', key: 'title', width: 200, ellipsis: { tooltip: true } },
  {
    title: '严重程度',
    key: 'severity',
    width: 100,
    render(row) {
      const s = severityMap[row.severity]
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '未知' })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const s = statusMap[row.status]
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '未知' })
    }
  },
  { title: '截止日期', key: 'dueDate', width: 120 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          row.status < 3 ? h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleExecute(row)
          }, { default: () => row.status === 0 ? '确认' : row.status === 1 ? '开始修复' : '标记修复' }) : null,
          h(NButton, {
            size: 'small',
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该Bug？'
          })
        ]
      })
    }
  }
]

async function loadProjects() {
  try {
    const { data } = await getProjectPage({ current: 1, size: 100 })
    projects.value = data.data.records
    projectOptions.value = data.data.records.map(p => ({ label: p.name, value: p.id }))
  } catch (error) {
    console.error('加载项目列表失败')
  }
}

async function loadBugs() {
  loading.value = true
  try {
    const { data } = await getBugPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      projectId: queryParams.value.projectId ?? undefined,
      title: queryParams.value.title || undefined,
      severity: queryParams.value.severity ?? undefined,
      status: queryParams.value.status ?? undefined
    })
    bugs.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载Bug列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadBugs()
}

function handleReset() {
  queryParams.value = { projectId: null, title: '', severity: null, status: null }
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  formData.value = { projectId: queryParams.value.projectId || undefined, title: '', description: '', severity: 1, assigneeId: undefined, dueDate: undefined }
  showModal.value = true
}

function handleEdit(row: BugVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    projectId: row.projectId,
    title: row.title,
    description: row.description,
    severity: row.severity,
    status: row.status,
    assigneeId: row.assigneeId,
    dueDate: row.dueDate
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    if (isEdit.value) {
      await updateBug(formData.value as BugUpdateParams)
      message.success('更新成功')
    } else {
      await createBug(formData.value)
      message.success('创建成功')
    }
    showModal.value = false
    loadBugs()
  } catch (error) {
    // validation or api error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteBug(id)
    message.success('删除成功')
    loadBugs()
  } catch (error) {
    message.error('删除失败')
  }
}

async function handleExecute(row: BugVO) {
  const nextStatus = row.status === 0 ? 1 : row.status === 1 ? 2 : 3
  const label = row.status === 0 ? '确认' : row.status === 1 ? '开始修复' : '标记修复'
  try {
    await updateBug({ id: row.id, title: row.title, description: row.description, severity: row.severity, status: nextStatus, assigneeId: row.assigneeId, dueDate: row.dueDate })
    message.success(`${label}成功`)
    loadBugs()
  } catch (error) {
    message.error(`${label}失败`)
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadBugs()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadBugs()
}

onMounted(() => {
  loadProjects()
  loadBugs()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">Bug管理</h2>
      <NButton type="primary" size="small" @click="handleAdd">
        新建Bug
      </NButton>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NSelect
        v-model:value="queryParams.projectId"
        :options="projectOptions"
        placeholder="所属项目"
        clearable
        size="small"
        class="w-40"
      />
      <NInput
        v-model:value="queryParams.title"
        placeholder="Bug标题"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NSelect
        v-model:value="queryParams.severity"
        :options="severityOptions"
        placeholder="严重程度"
        clearable
        size="small"
        class="w-32"
      />
      <NSelect
        v-model:value="queryParams.status"
        :options="statusOptions"
        placeholder="状态"
        clearable
        size="small"
        class="w-32"
      />
      <NButton size="small" type="primary" @click="handleSearch">搜索</NButton>
      <NButton size="small" @click="handleReset">重置</NButton>
    </div>

    <!-- Bug列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="bugs"
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
      :title="isEdit ? '编辑Bug' : '新建Bug'"
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
        <NFormItem label="所属项目">
          <NSelect
            v-model:value="formData.projectId"
            :options="projectOptions"
            placeholder="请选择所属项目（可选）"
            clearable
            :disabled="isEdit"
          />
        </NFormItem>
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入Bug标题" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" placeholder="请输入Bug描述" />
        </NFormItem>
        <NFormItem label="严重程度" path="severity">
          <NSelect v-model:value="formData.severity" :options="severityOptions" />
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
