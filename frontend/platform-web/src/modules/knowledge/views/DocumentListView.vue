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
  NInputNumber,
  NPopconfirm,
  NTag,
  NUpload,
  useMessage
} from 'naive-ui'
import type { DataTableColumns, FormInst, UploadFileInfo } from 'naive-ui'
import {
  getDocumentPage,
  uploadDocument,
  deleteDocument,
  reprocessDocument
} from '@/api/knowledge'
import type { DocumentVO, DocumentUploadParams } from '@/api/knowledge'

const message = useMessage()
const loading = ref(false)
const showUploadModal = ref(false)
const formRef = ref<FormInst | null>(null)
const uploadFile = ref<File | null>(null)

const queryParams = ref({
  title: '',
  fileType: null as string | null,
  status: null as number | null
})

const documents = ref<DocumentVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const formData = ref<DocumentUploadParams>({
  title: '',
  categoryId: undefined,
  chunkSize: 500,
  chunkOverlap: 50
})

const formRules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }]
}

const fileTypeOptions = [
  { label: 'PDF', value: 'pdf' },
  { label: 'Markdown', value: 'md' },
  { label: 'Word', value: 'docx' },
  { label: '文本', value: 'txt' }
]

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '就绪', value: 2 },
  { label: '失败', value: 3 }
]

const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
  0: { label: '待处理', type: 'info' },
  1: { label: '处理中', type: 'warning' },
  2: { label: '就绪', type: 'success' },
  3: { label: '失败', type: 'error' }
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const columns: DataTableColumns<DocumentVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '标题', key: 'title', width: 200, ellipsis: { tooltip: true } },
  {
    title: '文件类型',
    key: 'fileType',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: 'info' }, { default: () => row.fileType?.toUpperCase() || '-' })
    }
  },
  {
    title: '文件大小',
    key: 'fileSize',
    width: 100,
    render(row) {
      return formatFileSize(row.fileSize || 0)
    }
  },
  { title: '分块数', key: 'chunkCount', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const s = statusMap[row.status]
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '未知' })
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, {
            size: 'small',
            disabled: row.status === 1,
            onClick: () => handleReprocess(row.id)
          }, { default: () => '重新处理' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该文档？'
          })
        ]
      })
    }
  }
]

async function loadDocuments() {
  loading.value = true
  try {
    const { data } = await getDocumentPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      title: queryParams.value.title || undefined,
      fileType: queryParams.value.fileType || undefined,
      status: queryParams.value.status ?? undefined
    })
    documents.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadDocuments()
}

function handleReset() {
  queryParams.value = { title: '', fileType: null, status: null }
  handleSearch()
}

function handleUpload() {
  formData.value = { title: '', categoryId: undefined, chunkSize: 500, chunkOverlap: 50 }
  uploadFile.value = null
  showUploadModal.value = true
}

function handleFileChange(options: { fileList: UploadFileInfo[] }) {
  if (options.fileList.length > 0) {
    const file = options.fileList[0]
    uploadFile.value = file.file as File
    // 自动填充标题
    if (!formData.value.title && file.name) {
      formData.value.title = file.name.replace(/\.[^.]+$/, '')
    }
  } else {
    uploadFile.value = null
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    if (!uploadFile.value) {
      message.error('请选择文件')
      return
    }
    await uploadDocument(uploadFile.value, formData.value)
    message.success('上传成功，文档正在后台处理')
    showUploadModal.value = false
    loadDocuments()
  } catch (error) {
    // validation or api error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteDocument(id)
    message.success('删除成功')
    loadDocuments()
  } catch (error) {
    message.error('删除失败')
  }
}

async function handleReprocess(id: number) {
  try {
    await reprocessDocument(id)
    message.success('重新处理已开始')
    loadDocuments()
  } catch (error) {
    message.error('重新处理失败')
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadDocuments()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadDocuments()
}

onMounted(() => {
  loadDocuments()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">文档管理</h2>
      <NButton type="primary" size="small" @click="handleUpload">
        上传文档
      </NButton>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.title"
        placeholder="文档标题"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NSelect
        v-model:value="queryParams.fileType"
        :options="fileTypeOptions"
        placeholder="文件类型"
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

    <!-- 文档列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="documents"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :pagination="pagination"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- 上传弹窗 -->
    <NModal
      v-model:show="showUploadModal"
      title="上传文档"
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
        <NFormItem label="文件" path="file">
          <NUpload
            :max="1"
            accept=".pdf,.md,.docx,.txt"
            :default-upload="false"
            @change="handleFileChange"
          >
            <NButton>选择文件</NButton>
          </NUpload>
        </NFormItem>
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入文档标题" />
        </NFormItem>
        <NFormItem label="分块大小" path="chunkSize">
          <NInputNumber v-model:value="formData.chunkSize" :min="100" :max="2000" />
        </NFormItem>
        <NFormItem label="分块重叠" path="chunkOverlap">
          <NInputNumber v-model:value="formData.chunkOverlap" :min="0" :max="500" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showUploadModal = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">上传</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
