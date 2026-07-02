<template>
  <div>
    <!-- 搜索栏 -->
    <n-card style="margin-bottom: 16px">
      <n-form inline :model="queryForm">
        <n-form-item label="名称">
          <n-input v-model:value="queryForm.name" placeholder="请输入任务名称" clearable />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="queryForm.status" placeholder="请选择状态" clearable :options="statusOptions" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" @click="handleSearch">搜索</n-button>
          <n-button @click="handleReset" style="margin-left: 8px">重置</n-button>
        </n-form-item>
      </n-form>
    </n-card>

    <!-- 工具栏 -->
    <n-card>
      <div style="margin-bottom: 16px">
        <n-button type="primary" @click="handleAdd">
          <template #icon><n-icon><svg viewBox="0 0 24 24"><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg></n-icon></template>
          新增任务
        </n-button>
      </div>

      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="modalTitle" preset="dialog" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="任务名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入任务名称" />
        </n-form-item>
        <n-form-item label="报表" path="reportId">
          <n-select v-model:value="formData.reportId" placeholder="请选择报表" :options="reportOptions" filterable />
        </n-form-item>
        <n-form-item label="Cron表达式" path="cron">
          <n-input v-model:value="formData.cron" placeholder="请输入Cron表达式，如：0 0 9 * * ?" />
        </n-form-item>
        <n-form-item label="导出格式">
          <n-select v-model:value="formData.config.exportFormat" placeholder="请选择导出格式" :options="formatOptions" />
        </n-form-item>
        <n-form-item label="接收邮箱">
          <n-input v-model:value="formData.config.email" placeholder="请输入接收邮箱" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showModal = false">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { NButton, NTag, useMessage, useDialog } from 'naive-ui'
import { get, post, put, del } from '@/utils/request'

const message = useMessage()
const dialog = useDialog()

// 列表数据
const list = ref<any[]>([])
const loading = ref(false)

// 报表选项
const reportOptions = ref<any[]>([])

// 查询表单
const queryForm = reactive({
  name: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [10, 20, 50],
  showSizePicker: true,
  prefix: (info: any) => `共 ${info.itemCount ?? 0} 条`
})

// 状态选项
const statusOptions = [
  { label: '停止', value: 0 },
  { label: '运行', value: 1 },
  { label: '暂停', value: 2 }
]

// 格式选项
const formatOptions = [
  { label: 'Excel', value: 'excel' },
  { label: 'PDF', value: 'pdf' },
  { label: 'Word', value: 'word' }
]

// 表格列定义
const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 100, ellipsis: { tooltip: true } },
  { title: '任务名称', key: 'name', width: 150 },
  { title: '报表', key: 'reportName', width: 150 },
  { title: 'Cron', key: 'cron', width: 120 },
  { title: '状态', key: 'status', width: 80, render: (row) => {
    const statusMap: Record<number, { label: string; type: string }> = {
      0: { label: '停止', type: 'default' },
      1: { label: '运行', type: 'success' },
      2: { label: '暂停', type: 'warning' }
    }
    const status = statusMap[row.status] || { label: '未知', type: 'default' }
    return h(NTag, { type: status.type as any }, { default: () => status.label })
  }},
  { title: '上次执行', key: 'lastExecuteTime', width: 180 },
  { title: '下次执行', key: 'nextExecuteTime', width: 180 },
  { title: '操作', key: 'actions', width: 250, render: (row) => h('div', { style: 'display: flex; gap: 8px;' }, [
    h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
    h(NButton, { size: 'small', type: 'success', onClick: () => handleStart(row), disabled: row.status === 1 }, { default: () => '启动' }),
    h(NButton, { size: 'small', type: 'warning', onClick: () => handleStop(row), disabled: row.status === 0 }, { default: () => '停止' }),
    h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
  ]) }
]

// 弹窗控制
const showModal = ref(false)
const modalTitle = ref('新增任务')
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)
const editingId = ref<string | null>(null)

// 表单数据
const formData = reactive({
  name: '',
  reportId: null as string | null,
  cron: '',
  config: {
    exportFormat: 'excel',
    email: ''
  },
  remark: ''
})

// 表单验证规则
const rules: FormRules = {
  name: { required: true, message: '请输入任务名称', trigger: 'blur' },
  reportId: { required: true, message: '请选择报表', trigger: 'change' },
  cron: { required: true, message: '请输入Cron表达式', trigger: 'blur' }
}

// 获取报表列表
const fetchReportOptions = async () => {
  try {
    const data = await get('/report/reports', { pageNum: 1, pageSize: 100 })
    const records = data?.records || data?.list || []
    reportOptions.value = records.map((r: any) => ({
      label: r.name,
      value: r.id
    }))
  } catch (error) {
    console.error('获取报表列表失败:', error)
  }
}

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const data = await get('/report/schedules', queryForm)
    list.value = data?.records || data?.list || []
    pagination.itemCount = data?.total || 0
  } catch (error) {
    console.error('获取调度列表失败:', error)
    list.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  queryForm.pageNum = 1
  fetchList()
}

// 重置
const handleReset = () => {
  queryForm.name = ''
  queryForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  modalTitle.value = '新增任务'
  editingId.value = null
  formData.name = ''
  formData.reportId = null
  formData.cron = ''
  formData.config = { exportFormat: 'excel', email: '' }
  formData.remark = ''
  showModal.value = true
}

// 编辑
const handleEdit = (row: any) => {
  modalTitle.value = '编辑任务'
  editingId.value = row.id
  formData.name = row.name
  formData.reportId = row.reportId
  formData.cron = row.cron
  formData.config = row.config || { exportFormat: 'excel', email: '' }
  formData.remark = row.remark
  showModal.value = true
}

// 启动
const handleStart = (row: any) => {
  dialog.info({
    title: '确认启动',
    content: `确定要启动任务 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await post(`/report/schedules/${row.id}/start`)
        message.success('启动成功')
        fetchList()
      } catch {
        message.error('启动失败')
      }
    }
  })
}

// 停止
const handleStop = (row: any) => {
  dialog.warning({
    title: '确认停止',
    content: `确定要停止任务 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await post(`/report/schedules/${row.id}/stop`)
        message.success('停止成功')
        fetchList()
      } catch {
        message.error('停止失败')
      }
    }
  })
}

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除任务 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await del(`/report/schedules/${row.id}`)
        message.success('删除成功')
        fetchList()
      } catch {
        message.error('删除失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (editingId.value) {
      await put(`/report/schedules/${editingId.value}`, formData)
      message.success('更新成功')
    } else {
      await post('/report/schedules', formData)
      message.success('创建成功')
    }
    showModal.value = false
    fetchList()
  } catch {} finally {
    submitting.value = false
  }
}

// 页码变更
const handlePageChange = (page: number) => {
  pagination.page = page
  queryForm.pageNum = page
  fetchList()
}

// 每页条数变更
const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  queryForm.pageSize = pageSize
  queryForm.pageNum = 1
  pagination.page = 1
  fetchList()
}

onMounted(() => {
  fetchList()
  fetchReportOptions()
})
</script>
