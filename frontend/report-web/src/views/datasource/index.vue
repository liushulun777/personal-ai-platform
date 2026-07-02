<template>
  <div>
    <!-- 搜索栏 -->
    <n-card style="margin-bottom: 16px">
      <n-form inline :model="queryForm">
        <n-form-item label="名称" label-width="60" style="width: 220px;">
          <n-input v-model:value="queryForm.name" placeholder="请输入数据源名称" clearable />
        </n-form-item>
        <n-form-item label="类型" label-width="60" style="width: 200px;">
          <n-select v-model:value="queryForm.type" placeholder="请选择类型" clearable :options="typeOptions" />
        </n-form-item>
        <n-form-item label="状态" label-width="60" style="width: 180px;">
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
          新增数据源
        </n-button>
      </div>

      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :pagination="pagination"
        :row-class-name="rowClassName"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="modalTitle" preset="dialog" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入数据源名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="formData.type" placeholder="请选择类型" :options="typeOptions" @update:value="handleTypeChange" />
        </n-form-item>
        <n-form-item label="配置" path="config">
          <n-input v-model:value="configStr" type="textarea" :rows="6" placeholder="请输入JSON配置" />
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
import { ref, reactive, computed, onMounted, h } from 'vue'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { NButton, NTag, useMessage, useDialog } from 'naive-ui'
import { get, post, put, del } from '@/utils/request'

const message = useMessage()
const dialog = useDialog()

// 列表数据
const list = ref<any[]>([])
const loading = ref(false)

// 查询表单
const queryForm = reactive({
  name: '',
  type: undefined as string | undefined,
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

// 类型选项
const typeOptions = [
  { label: 'MySQL', value: 'MYSQL' },
  { label: 'PostgreSQL', value: 'POSTGRESQL' },
  { label: 'API接口', value: 'API' },
  { label: 'Excel文件', value: 'EXCEL' },
  { label: 'CSV文件', value: 'CSV' }
]

// 状态选项
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

// 行样式
const rowClassName = ({ row }: { row: any }) => {
  return row && editingId.value === row.id ? 'selected-row' : ''
}

// 表格列定义
const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 100, ellipsis: { tooltip: true } },
  { title: '名称', key: 'name', width: 150 },
  { title: '类型', key: 'type', width: 120 },
  { title: '状态', key: 'status', width: 80, render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '启用' : '禁用' }) },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'actions', width: 200, render: (row) => h('div', { style: 'display: flex; gap: 8px;' }, [
    h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
    h(NButton, { size: 'small', onClick: () => handleTest(row) }, { default: () => '测试' }),
    h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
  ]) }
]

// 弹窗控制
const showModal = ref(false)
const modalTitle = ref('新增数据源')
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)
const editingId = ref<string | null>(null)

// 表单数据
const formData = reactive({
  name: '',
  type: '',
  config: {} as any,
  remark: ''
})

const configStr = computed({
  get: () => JSON.stringify(formData.config, null, 2),
  set: (val) => {
    try {
      formData.config = JSON.parse(val)
    } catch {}
  }
})

// 表单验证规则
const rules: FormRules = {
  name: { required: true, message: '请输入数据源名称', trigger: 'blur' },
  type: { required: true, message: '请选择类型', trigger: 'change' },
  config: { required: true, message: '请输入配置', trigger: 'blur' }
}

// 类型变更
const handleTypeChange = (type: string) => {
  const configs: Record<string, any> = {
    MYSQL: { host: '', port: 3306, database: '', username: '', password: '' },
    POSTGRESQL: { host: '', port: 5432, database: '', username: '', password: '' },
    API: { url: '', method: 'GET', headers: {}, auth: {} },
    EXCEL: { filePath: '', sheetName: '' },
    CSV: { filePath: '' }
  }
  formData.config = configs[type] || {}
}

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const data: any = await get('/report/datasources', queryForm)
    list.value = data?.records  || []
    pagination.itemCount = data?.total || 0
  } catch (error) {
    console.error('获取数据源列表失败:', error)
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
  queryForm.type = undefined
  queryForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  modalTitle.value = '新增数据源'
  editingId.value = null
  formData.name = ''
  formData.type = ''
  formData.config = {}
  formData.remark = ''
  showModal.value = true
}

// 编辑
const handleEdit = (row: any) => {
  modalTitle.value = '编辑数据源'
  editingId.value = row.id
  formData.name = row.name
  formData.type = row.type
  formData.config = row.config || {}
  formData.remark = row.remark
  showModal.value = true
}

// 测试连接
const handleTest = async (row: any) => {
  try {
    const result = await post(`/report/datasources/${row.id}/test`)
    if (result) {
      message.success('连接成功')
    } else {
      message.error('连接失败')
    }
  } catch {
    message.error('测试失败')
  }
}

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除数据源 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await del(`/report/datasources/${row.id}`)
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
      await put(`/report/datasources/${editingId.value}`, formData)
      message.success('更新成功')
    } else {
      await post('/report/datasources', formData)
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
})
</script>

<style scoped>
:deep(.selected-row) {
  background-color: #e6f7ff !important;
}
</style>
