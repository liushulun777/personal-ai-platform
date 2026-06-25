<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import {
  NDataTable,
  NButton,
  NSpace,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPopconfirm,
  useMessage
} from 'naive-ui'
import { NTag } from 'naive-ui/es/tag'
import type { DataTableColumns, FormInst } from 'naive-ui'
import {
  getAllCategories,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'
import type { CategoryVO, CategoryCreateParams, CategoryUpdateParams } from '@/api/category'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const categories = ref<CategoryVO[]>([])

const formData = ref<CategoryCreateParams & { id?: number }>({
  name: '',
  slug: '',
  description: '',
  sort: 0,
  status: 1
})

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'error' }
}

const columns: DataTableColumns<CategoryVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', width: 120 },
  { title: 'Slug', key: 'slug', width: 120 },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const status = statusMap[row.status]
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.label })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该分类？'
          })
        ]
      })
    }
  }
]

async function loadCategories() {
  loading.value = true
  try {
    const { data } = await getAllCategories()
    categories.value = data.data
  } catch (error) {
    message.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  formData.value = { name: '', slug: '', description: '', sort: 0, status: 1 }
  showModal.value = true
}

function handleEdit(row: CategoryVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    name: row.name,
    slug: row.slug,
    description: row.description,
    sort: row.sort,
    status: row.status
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()

    if (isEdit.value && formData.value.id) {
      const updateData: CategoryUpdateParams = {
        name: formData.value.name,
        slug: formData.value.slug,
        description: formData.value.description,
        sort: formData.value.sort,
        status: formData.value.status
      }
      await updateCategory(formData.value.id, updateData)
      message.success('更新成功')
    } else {
      await createCategory(formData.value)
      message.success('创建成功')
    }

    showModal.value = false
    loadCategories()
  } catch (error) {
    // 表单验证失败或接口错误
  }
}

async function handleDelete(id: number) {
  try {
    await deleteCategory(id)
    message.success('删除成功')
    loadCategories()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">分类管理</h2>
      <NButton type="primary" size="small" @click="handleAdd">新增分类</NButton>
    </div>

    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable :columns="columns" :data="categories" :loading="loading" :bordered="false" :single-line="false" />
    </div>

    <NModal v-model:show="showModal" preset="dialog" title="分类信息">
      <NForm ref="formRef" :model="formData" label-placement="left" label-width="80">
        <NFormItem label="名称" path="name">
          <NInput v-model:value="formData.name" placeholder="请输入分类名称" />
        </NFormItem>
        <NFormItem label="Slug" path="slug">
          <NInput v-model:value="formData.slug" placeholder="请输入URL标识" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" placeholder="请输入描述" />
        </NFormItem>
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" />
        </NFormItem>
      </NForm>
      <template #action>
        <NButton @click="showModal = false">取消</NButton>
        <NButton type="primary" @click="handleSubmit">确定</NButton>
      </template>
    </NModal>
  </div>
</template>
