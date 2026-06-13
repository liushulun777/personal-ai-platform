<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import {
  NCard,
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
  useMessage
} from 'naive-ui'
import { NTag } from 'naive-ui/es/tag'
import type { DataTableColumns, FormInst } from 'naive-ui'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus
} from '@/api/user'
import type { UserVO, UserCreateParams, UserUpdateParams } from '@/api/user'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

// 查询参数
const queryParams = ref({
  username: '',
  nickname: '',
  status: null as number | null
})

// 用户列表
const users = ref<UserVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 表单数据
const formData = ref<UserCreateParams & { id?: number }>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 50, message: '用户名长度为4-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度为6-50个字符', trigger: 'blur' }
  ]
}

// 状态选项
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

// 状态映射
const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'error' }
}

// 表格列定义
const columns: DataTableColumns<UserVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username', width: 120 },
  { title: '昵称', key: 'nickname', width: 120 },
  { title: '邮箱', key: 'email', ellipsis: { tooltip: true } },
  { title: '手机号', key: 'phone', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const status = statusMap[row.status]
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.label })
    }
  },
  { title: '创建时间', key: 'createTime', width: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small',
            type: row.status === 1 ? 'warning' : 'success',
            onClick: () => handleToggleStatus(row)
          }, { default: () => row.status === 1 ? '禁用' : '启用' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该用户？'
          })
        ]
      })
    }
  }
]

// 加载用户列表
async function loadUsers() {
  loading.value = true
  try {
    const { data } = await getUserPage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      username: queryParams.value.username || undefined,
      nickname: queryParams.value.nickname || undefined,
      status: queryParams.value.status ?? undefined
    })
    users.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.value.page = 1
  loadUsers()
}

// 重置搜索
function handleReset() {
  queryParams.value = { username: '', nickname: '', status: null }
  handleSearch()
}

// 新建用户
function handleCreate() {
  isEdit.value = false
  formData.value = {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    status: 1
  }
  showModal.value = true
}

// 编辑用户
function handleEdit(row: UserVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    status: row.status
  }
  showModal.value = true
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()

    if (isEdit.value && formData.value.id) {
      const updateData: UserUpdateParams = {
        nickname: formData.value.nickname,
        email: formData.value.email,
        phone: formData.value.phone
      }
      await updateUser(formData.value.id, updateData)
      message.success('更新成功')
    } else {
      await createUser(formData.value)
      message.success('创建成功')
    }

    showModal.value = false
    loadUsers()
  } catch (error) {
    // 表单验证失败或接口错误
  }
}

// 切换用户状态
async function handleToggleStatus(row: UserVO) {
  try {
    await updateUserStatus(row.id, { status: row.status === 1 ? 0 : 1 })
    message.success('状态修改成功')
    loadUsers()
  } catch (error) {
    message.error('状态修改失败')
  }
}

// 删除用户
async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    message.success('删除成功')
    loadUsers()
  } catch (error) {
    message.error('删除失败')
  }
}

// 分页变化
function handlePageChange(page: number) {
  pagination.value.page = page
  loadUsers()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">用户管理</h2>
      <NButton type="primary" size="small" @click="handleCreate">
        新建用户
      </NButton>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.username"
        placeholder="用户名"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NInput
        v-model:value="queryParams.nickname"
        placeholder="昵称"
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
        class="w-28"
      />
      <NButton size="small" type="primary" @click="handleSearch">搜索</NButton>
      <NButton size="small" @click="handleReset">重置</NButton>
    </div>

    <!-- 用户列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="users"
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
      :title="isEdit ? '编辑用户' : '新建用户'"
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
        <NFormItem label="用户名" path="username">
          <NInput
            v-model:value="formData.username"
            :disabled="isEdit"
            placeholder="请输入用户名"
          />
        </NFormItem>
        <NFormItem v-if="!isEdit" label="密码" path="password">
          <NInput
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
          />
        </NFormItem>
        <NFormItem label="昵称" path="nickname">
          <NInput
            v-model:value="formData.nickname"
            placeholder="请输入昵称"
          />
        </NFormItem>
        <NFormItem label="邮箱" path="email">
          <NInput
            v-model:value="formData.email"
            placeholder="请输入邮箱"
          />
        </NFormItem>
        <NFormItem label="手机号" path="phone">
          <NInput
            v-model:value="formData.phone"
            placeholder="请输入手机号"
          />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSelect
            v-model:value="formData.status"
            :options="statusOptions"
          />
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
