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
  NTreeSelect,
  NPopconfirm,
  useMessage
} from 'naive-ui'
import type { DataTableColumns, FormInst, TreeSelectOption, SelectOption } from 'naive-ui'
import {
  getRolePage,
  createRole,
  updateRole,
  deleteRole
} from '@/api/role'
import type { RoleVO, RoleCreateParams, RoleUpdateParams } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import type { MenuVO } from '@/api/menu'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

// 查询参数
const queryParams = ref({
  roleName: '',
  roleKey: '',
  status: null as number | null
})

// 角色列表
const roles = ref<RoleVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 菜单树
const menuTree = ref<TreeSelectOption[]>([])

// 表单数据
const formData = ref<RoleCreateParams & { id?: number }>({
  roleName: '',
  roleKey: '',
  description: '',
  sort: 0,
  status: 1,
  menuIds: []
})

// 表单验证规则
const formRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请输入角色标识', trigger: 'blur' }
  ]
}

// 状态选项
const statusOptions: SelectOption[] = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

// 表格列定义
const columns: DataTableColumns<RoleVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '角色名称', key: 'roleName', width: 150 },
  { title: '角色标识', key: 'roleKey', width: 150 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该角色？'
          })
        ]
      })
    }
  }
]

// 转换菜单树为TreeSelect格式
function convertMenuTree(menus: MenuVO[]): TreeSelectOption[] {
  return menus.map(menu => ({
    key: menu.id,
    label: menu.menuName,
    children: menu.children ? convertMenuTree(menu.children) : undefined
  }))
}

// 加载菜单树
async function loadMenuTree() {
  try {
    const { data } = await getMenuTree()
    menuTree.value = convertMenuTree(data.data)
  } catch (error) {
    message.error('加载菜单树失败')
  }
}

// 加载角色列表
async function loadRoles() {
  loading.value = true
  try {
    const { data } = await getRolePage({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      roleName: queryParams.value.roleName || undefined,
      roleKey: queryParams.value.roleKey || undefined,
      status: queryParams.value.status ?? undefined
    })
    roles.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.value.page = 1
  loadRoles()
}

// 重置搜索
function handleReset() {
  queryParams.value = { roleName: '', roleKey: '', status: null }
  handleSearch()
}

// 新建角色
function handleCreate() {
  isEdit.value = false
  formData.value = {
    roleName: '',
    roleKey: '',
    description: '',
    sort: 0,
    status: 1,
    menuIds: []
  }
  showModal.value = true
}

// 编辑角色
async function handleEdit(row: RoleVO) {
  isEdit.value = true
  try {
    const { data } = await import('@/api/role').then(m => m.getRoleById(row.id))
    formData.value = {
      id: data.data.id,
      roleName: data.data.roleName,
      roleKey: data.data.roleKey,
      description: data.data.description,
      sort: data.data.sort,
      status: data.data.status,
      menuIds: data.data.menuIds
    }
    showModal.value = true
  } catch (error) {
    message.error('获取角色详情失败')
  }
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()

    if (isEdit.value && formData.value.id) {
      const updateData: RoleUpdateParams = {
        roleName: formData.value.roleName,
        roleKey: formData.value.roleKey,
        description: formData.value.description,
        sort: formData.value.sort,
        status: formData.value.status,
        menuIds: formData.value.menuIds
      }
      await updateRole(formData.value.id, updateData)
      message.success('更新成功')
    } else {
      await createRole(formData.value)
      message.success('创建成功')
    }

    showModal.value = false
    loadRoles()
  } catch (error) {
    // 表单验证失败或接口错误
  }
}

// 删除角色
async function handleDelete(id: number) {
  try {
    await deleteRole(id)
    message.success('删除成功')
    loadRoles()
  } catch (error) {
    message.error('删除失败')
  }
}

// 分页变化
function handlePageChange(page: number) {
  pagination.value.page = page
  loadRoles()
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadRoles()
}

onMounted(() => {
  loadRoles()
  loadMenuTree()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">角色管理</h2>
      <NButton type="primary" size="small" @click="handleCreate">
        新建角色
      </NButton>
    </div>

    <!-- 搜索区域 -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.roleName"
        placeholder="角色名称"
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
      />
      <NInput
        v-model:value="queryParams.roleKey"
        placeholder="角色标识"
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

    <!-- 角色列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="roles"
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
      :title="isEdit ? '编辑角色' : '新建角色'"
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
        <NFormItem label="角色名称" path="roleName">
          <NInput
            v-model:value="formData.roleName"
            placeholder="请输入角色名称"
          />
        </NFormItem>
        <NFormItem label="角色标识" path="roleKey">
          <NInput
            v-model:value="formData.roleKey"
            placeholder="请输入角色标识"
          />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput
            v-model:value="formData.description"
            type="textarea"
            placeholder="请输入描述"
          />
        </NFormItem>
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSelect
            v-model:value="formData.status"
            :options="statusOptions"
          />
        </NFormItem>
        <NFormItem label="菜单权限" path="menuIds">
          <NTreeSelect
            v-model:value="formData.menuIds"
            :options="menuTree"
            multiple
            checkable
            cascade
            placeholder="请选择菜单权限"
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
