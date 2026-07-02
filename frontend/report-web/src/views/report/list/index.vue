<template>
  <div>
    <!-- 搜索栏 -->
    <n-card style="margin-bottom: 16px">
      <n-form inline :model="queryForm">
        <n-form-item label="名称" label-width="60" style="width: 220px;">
          <n-input v-model:value="queryForm.name" placeholder="请输入报表名称" clearable />
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
          新增报表
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

    <!-- 新增弹窗 -->
    <n-modal v-model:show="showModal" title="新增报表" preset="dialog" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入报表名称" />
        </n-form-item>
        <n-form-item label="编码" path="code">
          <n-input v-model:value="formData.code" placeholder="请输入报表编码（英文）" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="formData.type" placeholder="请选择类型" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="分类">
          <n-input v-model:value="formData.category" placeholder="请输入分类" />
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
import { useRouter } from 'vue-router'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { NButton, NTag, useMessage, useDialog } from 'naive-ui'
import { get, post, del } from '@/utils/request'

const router = useRouter()
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
  { label: '普通报表', value: 'NORMAL' },
  { label: '复杂报表', value: 'COMPLEX' }
]

// 状态选项
const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已归档', value: 2 }
]

// 表格列定义
const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 100, ellipsis: { tooltip: true } },
  { title: '名称', key: 'name', width: 200 },
  { title: '编码', key: 'code', width: 120 },
  { title: '类型', key: 'type', width: 100 },
  { title: '版本', key: 'version', width: 80 },
  { title: '状态', key: 'status', width: 80, render: (row) => {
    const statusMap: Record<number, { label: string; type: string }> = {
      0: { label: '草稿', type: 'default' },
      1: { label: '已发布', type: 'success' },
      2: { label: '已归档', type: 'warning' }
    }
    const status = statusMap[row.status] || { label: '未知', type: 'default' }
    return h(NTag, { type: status.type as any }, { default: () => status.label })
  }},
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'actions', width: 250, render: (row) => h('div', { style: 'display: flex; gap: 8px;' }, [
    h(NButton, { size: 'small', onClick: () => handleDesign(row) }, { default: () => '设计' }),
    h(NButton, { size: 'small', onClick: () => handlePreview(row) }, { default: () => '预览' }),
    h(NButton, { size: 'small', onClick: () => handlePublish(row), disabled: row.status !== 0 }, { default: () => '发布' }),
    h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
  ]) }
]

// 弹窗控制
const showModal = ref(false)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)

// 表单数据
const formData = reactive({
  name: '',
  code: '',
  type: 'NORMAL',
  category: '',
  config: {},
  remark: ''
})

// 表单验证规则
const rules: FormRules = {
  name: { required: true, message: '请输入报表名称', trigger: 'blur' },
  code: { required: true, message: '请输入报表编码', trigger: 'blur' },
  type: { required: true, message: '请选择类型', trigger: 'change' }
}

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const data: any = await get('/report/reports', queryForm)
    list.value = data?.records || data?.list || []
    pagination.itemCount = data?.total || 0
  } catch (error) {
    console.error('获取报表列表失败:', error)
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
  formData.name = ''
  formData.code = ''
  formData.type = 'NORMAL'
  formData.category = ''
  formData.config = {}
  formData.remark = ''
  showModal.value = true
}

// 设计
const handleDesign = (row: any) => {
  router.push(`/report/designer/${row.id}`)
}

// 预览
const handlePreview = (row: any) => {
  router.push(`/report/preview/${row.id}`)
}

// 发布
const handlePublish = (row: any) => {
  dialog.info({
    title: '确认发布',
    content: `确定要发布报表 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await post(`/report/reports/${row.id}/publish`)
        message.success('发布成功')
        fetchList()
      } catch {
        message.error('发布失败')
      }
    }
  })
}

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除报表 "${row.name}" 吗？`,
    onPositiveClick: async () => {
      try {
        await del(`/report/reports/${row.id}`)
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
    const data: any = await post('/report/reports', formData)
    message.success('创建成功')
    showModal.value = false
    if (data) {
      router.push(`/report/designer/${data}`)
    } else {
      fetchList()
    }
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
