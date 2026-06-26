<script setup lang="ts">
import { ref, h, onMounted, type VNode } from 'vue'
import { useRoute } from 'vue-router'
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
  NTimeline,
  NTimelineItem,
  NTooltip,
  useMessage
} from 'naive-ui'
import type { DataTableColumns, FormInst, SelectOption } from 'naive-ui'
import {
  getTaskPage,
  createTask,
  updateTask,
  deleteTask,
  startTask,
  completeTask,
  submitReview,
  approveTask,
  rejectTask,
  blockTask,
  unblockTask,
  getTaskExecutions,
  executeAgentTask,
  getProjectPage
} from '@/api/project'
import type { TaskVO, TaskCreateParams, TaskUpdateParams, TaskExecutionVO, ProjectVO } from '@/api/project'

const route = useRoute()
const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const showExecutionModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const executingTaskId = ref<number | null>(null)

const queryParams = ref({
  projectId: route.query.projectId ? route.query.projectId as string : null as string | null,
  title: '',
  status: null as number | null,
  priority: null as number | null
})

const tasks = ref<TaskVO[]>([])
const executions = ref<TaskExecutionVO[]>([])
const projects = ref<ProjectVO[]>([])
const currentTaskId = ref<number | null>(null)
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const formData = ref<TaskCreateParams & { id?: number; status?: number }>({
  projectId: 0,
  title: '',
  description: '',
  priority: 1,
  assigneeId: undefined,
  dueDate: undefined
})

const formRules = {
  projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

// 状态定义
const statusOptions: SelectOption[] = [
  { label: 'BACKLOG', value: 0 },
  { label: 'READY', value: 1 },
  { label: 'DOING', value: 2 },
  { label: 'REVIEW', value: 3 },
  { label: 'DONE', value: 4 },
  { label: 'BLOCKED', value: 5 }
]

const priorityOptions: SelectOption[] = [
  { label: 'LOW', value: 0 },
  { label: 'MEDIUM', value: 1 },
  { label: 'HIGH', value: 2 },
  { label: 'URGENT', value: 3 }
]

const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' | 'default' }> = {
  0: { label: 'BACKLOG', type: 'default' },
  1: { label: 'READY', type: 'info' },
  2: { label: 'DOING', type: 'warning' },
  3: { label: 'REVIEW', type: 'info' },
  4: { label: 'DONE', type: 'success' },
  5: { label: 'BLOCKED', type: 'error' }
}

const priorityMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: 'LOW', type: 'success' },
  1: { label: 'MEDIUM', type: 'info' },
  2: { label: 'HIGH', type: 'warning' },
  3: { label: 'URGENT', type: 'error' }
}

const sourceTypeMap: Record<string, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  'MANUAL': { label: '人工', type: 'info' },
  'AI_GENERATED': { label: 'AI', type: 'warning' },
  'AGENT_CREATED': { label: 'Agent', type: 'success' }
}

// 项目选项
const projectOptions = ref<{ label: string; value: number }[]>([])

// 获取项目名称
function getProjectName(projectId: number): string {
  const project = projects.value.find(p => p.id === projectId)
  return project ? project.name : String(projectId)
}

// 判断任务是否可以执行（前置任务需要完成）
function canExecuteTask(task: TaskVO): boolean {
  // 只有 BACKLOG 和 READY 状态的任务可以执行
  return task.status === 0 || task.status === 1
}

// 判断任务是否可以编辑/删除
function canEditTask(task: TaskVO): boolean {
  // 只有 BACKLOG、READY、BLOCKED 状态的任务可以编辑
  return task.status === 0 || task.status === 1 || task.status === 5
}

const columns: DataTableColumns<TaskVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return h('span', {}, { default: () => (pagination.value.page - 1) * pagination.value.pageSize + index + 1 })
    }
  },
  {
    title: '排序',
    key: 'sortOrder',
    width: 60,
    render(row) {
      return h('span', {}, { default: () => row.sortOrder || '-' })
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
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const s = statusMap[row.status]
      const content = [h('span', {}, { default: () => s?.label || '未知' })]

      // 如果是 BLOCKED 状态，显示阻塞原因
      if (row.status === 5 && row.blockedReason) {
        content.push(
          h(NTooltip, { trigger: 'hover' }, {
            trigger: () => h('span', { class: 'ml-1 cursor-help' }, { default: () => '⚠️' }),
            default: () => row.blockedReason
          })
        )
      }

      return h(NTag, { type: s?.type, size: 'small' }, { default: () => content })
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
  {
    title: '来源',
    key: 'sourceType',
    width: 80,
    render(row) {
      const s = sourceTypeMap[row.sourceType || 'MANUAL']
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '人工' })
    }
  },
  {
    title: '预估工时',
    key: 'estimatedHours',
    width: 80,
    render(row) {
      return h('span', {}, { default: () => row.estimatedHours ? `${row.estimatedHours}h` : '-' })
    }
  },
  { title: '截止日期', key: 'dueDate', width: 120 },
  {
    title: '操作',
    key: 'actions',
    width: 400,
    render(row) {
      const actions: VNode[] = []

      // Agent 执行按钮（AI_GENERATED 任务，且状态为 BACKLOG 或 READY）
      if (row.sourceType === 'AI_GENERATED' && canExecuteTask(row)) {
        actions.push(
          h(NTooltip, { trigger: 'hover' }, {
            trigger: () => h(NButton, {
              size: 'small',
              type: 'warning',
              loading: executingTaskId.value === row.id,
              onClick: () => handleAgentExecute(row.id)
            }, { default: () => 'Agent执行' }),
            default: () => '触发 Agent 自动生成代码'
          })
        )
      }

      // 手动开始任务（BACKLOG/READY -> DOING）
      if (row.status === 0 || row.status === 1) {
        actions.push(
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleStart(row.id)
          }, { default: () => '开始' })
        )
      }

      // DOING 状态的操作
      if (row.status === 2) {
        actions.push(
          h(NButton, {
            size: 'small',
            type: 'success',
            onClick: () => handleComplete(row.id)
          }, { default: () => '完成' }),
          h(NButton, {
            size: 'small',
            type: 'info',
            onClick: () => handleSubmitReview(row.id)
          }, { default: () => '提交审核' }),
          h(NPopconfirm, {
            onPositiveClick: () => handleBlockWithReason(row.id)
          }, {
            trigger: () => h(NButton, { size: 'small', type: 'warning' }, { default: () => '阻塞' }),
            default: () => '确认阻塞该任务？'
          })
        )
      }

      // REVIEW 状态的操作
      if (row.status === 3) {
        actions.push(
          h(NButton, {
            size: 'small',
            type: 'success',
            onClick: () => handleApprove(row.id)
          }, { default: () => '通过' }),
          h(NButton, {
            size: 'small',
            type: 'error',
            onClick: () => handleReject(row.id)
          }, { default: () => '拒绝' })
        )
      }

      // BLOCKED 状态的操作
      if (row.status === 5) {
        actions.push(
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleUnblock(row.id)
          }, { default: () => '解除阻塞' })
        )
      }

      // 查看执行日志
      actions.push(
        h(NButton, {
          size: 'small',
          quaternary: true,
          type: 'info',
          onClick: () => handleViewExecutions(row.id)
        }, { default: () => '日志' })
      )

      // 编辑按钮（仅可编辑状态显示）
      if (canEditTask(row)) {
        actions.push(
          h(NButton, {
            size: 'small',
            quaternary: true,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' })
        )
      }

      // 删除按钮（仅 BACKLOG 状态可删除）
      if (row.status === 0) {
        actions.push(
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该任务？'
          })
        )
      }

      return h(NSpace, { size: 4 }, { default: () => actions })
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

async function loadTasks() {
  loading.value = true
  try {
    const { data } = await getTaskPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      projectId: queryParams.value.projectId ?? undefined,
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
  queryParams.value = { projectId: null, title: '', status: null, priority: null }
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  formData.value = { projectId: queryParams.value.projectId || 0, title: '', description: '', priority: 1, assigneeId: undefined, dueDate: undefined }
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
      await createTask(formData.value as TaskCreateParams)
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

// Agent 执行任务
async function handleAgentExecute(taskId: number) {
  executingTaskId.value = taskId
  try {
    await executeAgentTask(taskId)
    message.success('Agent 任务已触发，请查看执行日志')
    loadTasks()
  } catch (error) {
    message.error('Agent 执行失败')
  } finally {
    executingTaskId.value = null
  }
}

// 开始任务
async function handleStart(id: number) {
  try {
    await startTask(id)
    message.success('任务已开始')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 完成任务
async function handleComplete(id: number) {
  try {
    await completeTask(id)
    message.success('任务已完成')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 提交审核
async function handleSubmitReview(id: number) {
  try {
    await submitReview(id)
    message.success('已提交审核')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 审核通过
async function handleApprove(id: number) {
  try {
    await approveTask(id)
    message.success('审核通过')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 审核拒绝
async function handleReject(id: number) {
  try {
    await rejectTask(id, '审核不通过')
    message.success('已拒绝')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 阻塞任务
async function handleBlockWithReason(id: number) {
  try {
    await blockTask(id, '手动阻塞')
    message.success('任务已阻塞')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 解除阻塞
async function handleUnblock(id: number) {
  try {
    await unblockTask(id)
    message.success('已解除阻塞')
    loadTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 查看执行日志
async function handleViewExecutions(taskId: number) {
  currentTaskId.value = taskId
  try {
    const { data } = await getTaskExecutions(taskId)
    executions.value = data.data || []
    showExecutionModal.value = true
  } catch (error) {
    message.error('获取执行日志失败')
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
  loadProjects()
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
      <NSelect
        v-model:value="queryParams.projectId"
        :options="projectOptions"
        placeholder="所属项目"
        clearable
        filterable
        size="small"
        class="w-48"
      />
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
        <NFormItem label="所属项目" path="projectId">
          <NSelect
            v-model:value="formData.projectId"
            :options="projectOptions"
            placeholder="请选择项目"
            :disabled="isEdit"
          />
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

    <!-- 执行日志弹窗 -->
    <NModal
      v-model:show="showExecutionModal"
      title="执行日志"
      preset="card"
      style="width: 600px"
    >
      <div v-if="executions.length === 0" class="text-center py-8" style="color: var(--text-muted)">
        暂无执行记录
      </div>
      <NTimeline v-else>
        <NTimelineItem
          v-for="exec in executions"
          :key="exec.id"
          :type="exec.status === 2 ? 'success' : exec.status === 3 ? 'error' : 'info'"
          :title="exec.action"
          :content="exec.result || exec.errorMsg"
          :time="exec.createTime"
        />
      </NTimeline>
    </NModal>
  </div>
</template>
