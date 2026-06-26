<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">Prompt 模板管理</h2>
      <NButton v-if="hasPermission('ai:prompt:add')" type="primary" size="small" @click="handleAdd">
        新建模板
      </NButton>
    </div>

    <NCard>
      <NDataTable
        :columns="columns"
        :data="prompts"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: PromptVO) => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </NCard>

    <!-- 创建/编辑模板弹窗 -->
    <NModal
      v-model:show="showCreateModal"
      preset="dialog"
      :title="isEdit ? '编辑 Prompt 模板' : '新建 Prompt 模板'"
      :positive-text="isEdit ? '保存' : '创建'"
      negative-text="取消"
      :loading="creating"
      @positive-click="handleSubmit"
    >
      <NForm ref="formRef" :model="formData" label-placement="left" label-width="80">
        <NFormItem label="名称" path="name" :rule="{ required: true, message: '请输入模板名称' }">
          <NInput v-model:value="formData.name" placeholder="请输入模板名称" />
        </NFormItem>
        <NFormItem label="分类" path="category">
          <NSelect
            v-model:value="formData.category"
            :options="categoryOptions"
            placeholder="请选择分类"
            clearable
          />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" placeholder="请输入模板描述" />
        </NFormItem>
        <NFormItem label="Prompt" path="promptText" :rule="{ required: true, message: '请输入 Prompt 内容' }">
          <NInput
            v-model:value="formData.promptText"
            type="textarea"
            placeholder="请输入 Prompt 内容"
            :rows="6"
          />
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
import { getPrompts, createPrompt, updatePrompt, deletePrompt } from '@/api/ai'
import type { PromptVO, PromptCreateDTO } from '@/types/ai'
import { usePermission } from '@/composables/usePermission'

const message = useMessage()
const { hasPermission } = usePermission()
const dialog = useDialog()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const creating = ref(false)
const showCreateModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const prompts = ref<PromptVO[]>([])

const formData = reactive<PromptCreateDTO>({
  name: '',
  description: '',
  promptText: '',
  category: undefined,
})

const categoryOptions = [
  { label: '写作', value: 'writing' },
  { label: '翻译', value: 'translation' },
  { label: '总结', value: 'summary' },
  { label: '代码', value: 'code' },
  { label: '其他', value: 'other' },
]

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const columns: DataTableColumns<PromptVO> = [
  { title: '名称', key: 'name', width: 150 },
  {
    title: '分类',
    key: 'category',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: 'info' }, { default: () => row.category || '-' })
    },
  },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: 'Prompt',
    key: 'promptText',
    ellipsis: { tooltip: true },
    width: 300,
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      const buttons: any[] = []
      if (hasPermission('ai:prompt:edit')) buttons.push(h(NButton, { size: 'tiny', quaternary: true, onClick: () => handleEdit(row) }, { default: () => '编辑' }))
      if (hasPermission('ai:prompt:delete')) buttons.push(h(NButton, { size: 'tiny', type: 'error', quaternary: true, onClick: () => handleDelete(row) }, { default: () => '删除' }))
      return h(NSpace, { size: 4 }, { default: () => buttons })
    },
  },
]

async function loadPrompts() {
  loading.value = true
  try {
    const res = await getPrompts({
      current: pagination.page || 1,
      size: pagination.pageSize || 10,
    })
    const pageData = res.data.data
    prompts.value = pageData?.records || []
    pagination.itemCount = pageData?.total
  } catch {
    prompts.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  showCreateModal.value = true
}

function handleEdit(row: PromptVO) {
  isEdit.value = true
  editingId.value = row.id
  formData.name = row.name
  formData.description = row.description || ''
  formData.promptText = row.promptText
  formData.category = row.category
  showCreateModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return false
  }

  creating.value = true
  try {
    if (isEdit.value && editingId.value) {
      await updatePrompt(editingId.value, formData)
      message.success('更新成功')
    } else {
      await createPrompt(formData)
      message.success('创建成功')
    }
    showCreateModal.value = false
    resetForm()
    await loadPrompts()
    return true
  } catch {
    // interceptor handles error message
    return false
  } finally {
    creating.value = false
  }
}

function handleDelete(row: PromptVO) {
  dialog.warning({
    title: '删除模板',
    content: `确定要删除模板 "${row.name}" 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePrompt(row.id)
        message.success('删除成功')
        await loadPrompts()
      } catch {
        // interceptor handles error message
      }
    },
  })
}

function resetForm() {
  formData.name = ''
  formData.description = ''
  formData.promptText = ''
  formData.category = undefined
}

function handlePageChange(page: number) {
  pagination.page = page
  loadPrompts()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadPrompts()
}

onMounted(() => {
  loadPrompts()
})
</script>
