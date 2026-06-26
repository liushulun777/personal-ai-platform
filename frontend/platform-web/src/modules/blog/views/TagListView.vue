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
  NColorPicker,
  NPopconfirm
} from 'naive-ui'
import { NTag } from 'naive-ui/es/tag'
import type { DataTableColumns, FormInst } from 'naive-ui'
import {
  getAllTags,
  createTag,
  updateTag,
  deleteTag
} from '@/api/tag'
import type { TagVO, TagCreateParams, TagUpdateParams } from '@/api/tag'
import { usePermission } from '@/composables/usePermission'

const message = useMessage()
const { hasPermission } = usePermission()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const tags = ref<TagVO[]>([])

const formData = ref<TagCreateParams & { id?: number }>({
  name: '',
  slug: '',
  description: '',
  color: '#333333',
  sort: 0,
  status: 1
})

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'error' }
}

const columns: DataTableColumns<TagVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', width: 120 },
  { title: 'Slug', key: 'slug', width: 120 },
  {
    title: '颜色',
    key: 'color',
    width: 100,
    render(row) {
      return h('div', {
        style: {
          width: '24px',
          height: '24px',
          borderRadius: '4px',
          backgroundColor: row.color
        }
      })
    }
  },
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
      const buttons: any[] = []
      if (hasPermission('blog:tag:edit')) buttons.push(h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }))
      if (hasPermission('blog:tag:delete')) buttons.push(h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除该标签？'
      }))
      return h(NSpace, { size: 'small' }, { default: () => buttons })
    }
  }
]

async function loadTags() {
  loading.value = true
  try {
    const { data } = await getAllTags()
    tags.value = data.data
  } catch (error) {
    message.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  formData.value = { name: '', slug: '', description: '', color: '#333333', sort: 0, status: 1 }
  showModal.value = true
}

function handleEdit(row: TagVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    name: row.name,
    slug: row.slug,
    description: row.description,
    color: row.color,
    sort: row.sort,
    status: row.status
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()

    if (isEdit.value && formData.value.id) {
      const updateData: TagUpdateParams = {
        name: formData.value.name,
        slug: formData.value.slug,
        description: formData.value.description,
        color: formData.value.color,
        sort: formData.value.sort,
        status: formData.value.status
      }
      await updateTag(formData.value.id, updateData)
      message.success('更新成功')
    } else {
      await createTag(formData.value)
      message.success('创建成功')
    }

    showModal.value = false
    loadTags()
  } catch (error) {
    // 表单验证失败或接口错误
  }
}

async function handleDelete(id: number) {
  try {
    await deleteTag(id)
    message.success('删除成功')
    loadTags()
  } catch {
    // interceptor handles error message
  }
}

onMounted(() => {
  loadTags()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">标签管理</h2>
      <NButton v-if="hasPermission('blog:tag:add')" type="primary" size="small" @click="handleAdd">新增标签</NButton>
    </div>

    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable :columns="columns" :data="tags" :loading="loading" :bordered="false" :single-line="false" />
    </div>

    <NModal v-model:show="showModal" preset="dialog" title="标签信息">
      <NForm ref="formRef" :model="formData" label-placement="left" label-width="80">
        <NFormItem label="名称" path="name">
          <NInput v-model:value="formData.name" placeholder="请输入标签名称" />
        </NFormItem>
        <NFormItem label="Slug" path="slug">
          <NInput v-model:value="formData.slug" placeholder="请输入URL标识" />
        </NFormItem>
        <NFormItem label="颜色" path="color">
          <NColorPicker v-model:value="formData.color" />
        </NFormItem>
      </NForm>
      <template #action>
        <NButton @click="showModal = false">取消</NButton>
        <NButton type="primary" @click="handleSubmit">确定</NButton>
      </template>
    </NModal>
  </div>
</template>
