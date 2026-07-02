<template>
  <div>
    <!-- 搜索栏 -->
    <n-card style="margin-bottom: 16px">
      <n-form inline :model="queryForm">
        <n-form-item label="名称" label-width="60" style="width: 220px;">
          <n-input v-model:value="queryForm.name" placeholder="请输入数据集名称" clearable />
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
          新增数据集
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
    <n-modal v-model:show="showModal" :title="modalTitle" preset="dialog" style="width: 800px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入数据集名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="formData.type" placeholder="请选择类型" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="数据源" path="sourceId">
          <n-select v-model:value="formData.sourceId" placeholder="请选择数据源" :options="dataSourceOptions" />
        </n-form-item>
        <n-form-item v-if="formData.type === 'SQL'" label="SQL" path="config.sql">
          <n-input v-model:value="formData.config.sql" type="textarea" :rows="8" placeholder="请输入SQL语句" />
        </n-form-item>
        <n-form-item v-if="formData.type === 'API'" label="API配置">
          <n-input v-model:value="apiConfigStr" type="textarea" :rows="6" placeholder="请输入API配置JSON" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
        </n-form-item>
      </n-form>

      <!-- 数据预览 -->
      <n-divider>数据预览</n-divider>
      <n-data-table
        :columns="previewColumns"
        :data="previewData"
        :loading="previewLoading"
        max-height="300"
        size="small"
      />

      <template #action>
        <n-button @click="handlePreview">预览</n-button>
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

// 预览数据
const previewData = ref<any[]>([])
const previewLoading = ref(false)

// 数据源选项
const dataSourceOptions = ref<any[]>([])

// 查询表单
const queryForm = reactive({
  name: '',
  type: undefined as string | undefined,
  sourceId: undefined as string | undefined,
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
  { label: 'SQL数据集', value: 'SQL' },
  { label: 'API数据集', value: 'API' }
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
  { title: '类型', key: 'type', width: 100 },
  { title: '数据源', key: 'sourceName', width: 120 },
  { title: '状态', key: 'status', width: 80, render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '启用' : '禁用' }) },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'actions', width: 200, render: (row) => h('div', { style: 'display: flex; gap: 8px;' }, [
    h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
    h(NButton, { size: 'small', onClick: () => handlePreviewData(row) }, { default: () => '预览' }),
    h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
  ]) }
]

// 预览列
const previewColumns = computed(() => {
  if (previewData.value.length === 0) return []
  const keys = Object.keys(previewData.value[0])
  return keys.map(key => ({ title: key, key, width: 120 }))
})

// 弹窗控制
const showModal = ref(false)
const modalTitle = ref('新增数据集')
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)
const editingId = ref<string | null>(null)

// 表单数据
const formData = reactive({
  name: '',
  type: 'SQL',
  sourceId: null as string | null,
  config: {} as any,
  remark: ''
})

const apiConfigStr = computed({
  get: () => JSON.stringify(formData.config, null, 2),
  set: (val) => {
    try {
      formData.config = JSON.parse(val)
    } catch {}
  }
})

// 表单验证规则
const rules: FormRules = {
  name: { required: true, message: '请输入数据集名称', trigger: 'blur' },
  type: { required: true, message: '请选择类型', trigger: 'change' },
  sourceId: { required: true, message: '请选择数据源', trigger: 'change' }
}

// 获取数据源列表
const fetchDataSourceOptions = async () => {
  try {
    const data: any = await get('/report/datasources', { pageNum: 1, pageSize: 100 })
    const records = data?.records || data?.list || []
    dataSourceOptions.value = records.map((ds: any) => ({
      label: ds.name,
      value: ds.id
    }))
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  }
}

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const data: any = await get('/report/datasets', queryForm)
    list.value = data?.records || data?.list || []
    pagination.itemCount = data?.total || 0
  } catch (error) {
    console.error('获取数据集列表失败:', error)
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
  queryForm.sourceId = undefined
  queryForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  modalTitle.value = '新增数据集'
  editingId.value = null
  formData.name = ''
  formData.type = 'SQL'
  formData.sourceId = null
  formData.config = {}
  formData.remark = ''
  previewData.value = []
  showModal.value = true
}

// 编辑
const handleEdit = (row: any) => {
  modalTitle.value = '编辑数据集'
  editingId.value = row.id
  formData.name = row.name
  formData.type = row.type
  formData.sourceId = row.sourceId
  formData.config = row.config || {}
  formData.remark = row.remark
  previewData.value = []
  showModal.value = true
}

// 预览数据（新建时）
const handlePreview = async () => {
  if (!formData.sourceId) {
    message.warning('请先选择数据源')
    return
  }
  previewLoading.value = true
  try {
    const data: any = await post('/report/datasets/preview', {
      sourceId: formData.sourceId,
      config: formData.config
    })
    previewData.value = data || []
  } catch (error) {
    console.error('预览失败:', error)
    previewData.value = []
    message.error('预览失败')
  } finally {
    previewLoading.value = false
  }
}

// 预览已有数据集
const handlePreviewData = async (row: any) => {
  previewLoading.value = true
  editingId.value = row.id
  try {
    const data: any = await post(`/report/datasets/${row.id}/preview`, {})
    previewData.value = data || []
    // 显示预览弹窗
    formData.name = row.name
    formData.type = row.type
    formData.sourceId = row.sourceId
    formData.config = row.config || {}
    modalTitle.value = '预览数据集'
    showModal.value = true
  } catch (error) {
    console.error('预览失败:', error)
    message.error('预览失败')
  } finally {
    previewLoading.value = false
  }
}

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除数据集 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await del(`/report/datasets/${row.id}`)
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
      await put(`/report/datasets/${editingId.value}`, formData)
      message.success('更新成功')
    } else {
      await post('/report/datasets', formData)
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
  fetchDataSourceOptions()
})
</script>

<style scoped>
:deep(.selected-row) {
  background-color: #e6f7ff !important;
}
</style>
