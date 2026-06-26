<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import {
  NButton,
  NSpace,
  NInput,
  NInputNumber,
  NSelect,
  NModal,
  NForm,
  NFormItem,
  NCheckbox,
  NPopconfirm,
  NEmpty,
  NScrollbar,
  useMessage
} from 'naive-ui'
import type { FormInst, SelectOption } from 'naive-ui'
import {
  getRolePage,
  getRoleById,
  createRole,
  updateRole,
  deleteRole,
  assignMenus
} from '@/api/role'
import type { RoleVO, RoleCreateParams, RoleDetailVO } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import type { MenuVO } from '@/api/menu'
import { usePermission } from '@/composables/usePermission'

const message = useMessage()
const { hasPermission } = usePermission()
const loading = ref(false)
const saving = ref(false)

// ========== 角色列表 ==========
const roles = ref<RoleVO[]>([])
const selectedRole = ref<RoleDetailVO | null>(null)
const selectedRoleId = ref<number | null>(null)
const roleMenuIds = ref<Set<number>>(new Set())

// 查询参数
const queryParams = ref({
  roleName: '',
  roleKey: '',
  status: null as number | null
})

const statusOptions: SelectOption[] = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const dataScopeOptions: SelectOption[] = [
  { label: '全部数据', value: 1 },
  { label: '仅本人', value: 2 }
]

async function loadRoles() {
  loading.value = true
  try {
    const { data } = await getRolePage({
      current: 1,
      size: 100,
      roleName: queryParams.value.roleName || undefined,
      roleKey: queryParams.value.roleKey || undefined,
      status: queryParams.value.status ?? undefined
    })
    roles.value = data.data.records
    // 自动选中第一个角色
    if (roles.value.length && !selectedRoleId.value) {
      selectRole(roles.value[0])
    }
  } catch {
    message.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

async function selectRole(role: RoleVO) {
  selectedRoleId.value = role.id
  try {
    const { data } = await getRoleById(role.id)
    selectedRole.value = data.data
    roleMenuIds.value = new Set(data.data.menuIds || [])
  } catch {
    // interceptor handles error message
  }
}

// ========== 菜单权限表格 ==========
const menuTree = ref<MenuVO[]>([])

async function loadMenuTree() {
  try {
    const { data } = await getMenuTree()
    menuTree.value = data.data || []
  } catch {
    message.error('加载菜单树失败')
  }
}

// 构建表格数据：每个目录 → 下面的菜单 → 每个菜单的按钮
interface MenuColumn {
  dirId: number
  dirName: string
  menus: MenuRow[]
}

interface MenuRow {
  id: number
  name: string
  buttons: ButtonCell[]
}

interface ButtonCell {
  id: number
  name: string
  permission: string
}

const tableData = computed<MenuColumn[]>(() => {
  return menuTree.value
    .filter(dir => dir.menuType === 0)
    .map(dir => ({
      dirId: dir.id,
      dirName: dir.menuName,
      menus: (dir.children || [])
        .filter(menu => menu.menuType === 1)
        .map(menu => ({
          id: menu.id,
          name: menu.menuName,
          buttons: (menu.children || [])
            .filter(btn => btn.menuType === 2)
            .map(btn => ({
              id: btn.id,
              name: btn.menuName,
              permission: btn.permission || ''
            }))
        }))
    }))
    .filter(col => col.menus.length > 0)
})

// 判断菜单/按钮是否选中
function isChecked(id: number): boolean {
  return roleMenuIds.value.has(id)
}

// 判断目录是否全选
function isDirChecked(dirId: number, col: MenuColumn): boolean {
  const allIds = getAllIds(col)
  return allIds.length > 0 && allIds.every(id => roleMenuIds.value.has(id))
}

// 判断目录是否半选
function isDirIndeterminate(dirId: number, col: MenuColumn): boolean {
  const allIds = getAllIds(col)
  const checkedCount = allIds.filter(id => roleMenuIds.value.has(id)).length
  return checkedCount > 0 && checkedCount < allIds.length
}

// 获取目录下所有 ID（菜单 + 按钮）
function getAllIds(col: MenuColumn): number[] {
  const ids: number[] = []
  for (const menu of col.menus) {
    ids.push(menu.id)
    for (const btn of menu.buttons) {
      ids.push(btn.id)
    }
  }
  return ids
}

// 获取菜单下所有按钮 ID
function getMenuButtonIds(menu: MenuRow): number[] {
  return menu.buttons.map(b => b.id)
}

// 切换目录选中
function toggleDir(col: MenuColumn) {
  const allIds = getAllIds(col)
  const allChecked = allIds.length > 0 && allIds.every(id => roleMenuIds.value.has(id))
  if (allChecked) {
    allIds.forEach(id => roleMenuIds.value.delete(id))
  } else {
    allIds.forEach(id => roleMenuIds.value.add(id))
  }
}

// 切换菜单选中
function toggleMenu(menu: MenuRow) {
  const ids = [menu.id, ...getMenuButtonIds(menu)]
  const allChecked = ids.every(id => roleMenuIds.value.has(id))
  if (allChecked) {
    ids.forEach(id => roleMenuIds.value.delete(id))
  } else {
    ids.forEach(id => roleMenuIds.value.add(id))
  }
}

// 切换单个按钮
function toggleButton(id: number) {
  if (roleMenuIds.value.has(id)) {
    roleMenuIds.value.delete(id)
  } else {
    roleMenuIds.value.add(id)
  }
}

// 全选/全不选
function selectAll() {
  const allIds = tableData.value.flatMap(col => getAllIds(col))
  allIds.forEach(id => roleMenuIds.value.add(id))
}

function deselectAll() {
  roleMenuIds.value.clear()
}

// 保存权限
async function handleSavePermissions() {
  if (!selectedRoleId.value) return
  saving.value = true
  try {
    await assignMenus(selectedRoleId.value, Array.from(roleMenuIds.value))
    message.success('权限保存成功')
    // 刷新角色详情
    const { data } = await getRoleById(selectedRoleId.value)
    selectedRole.value = data.data
  } catch {
    // interceptor handles error message
  } finally {
    saving.value = false
  }
}

// ========== 角色编辑弹窗 ==========
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const formData = ref<RoleCreateParams & { id?: number }>({
  roleName: '',
  roleKey: '',
  description: '',
  sort: 0,
  status: 1,
  dataScope: 1,
  menuIds: []
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

function handleCreate() {
  isEdit.value = false
  formData.value = { roleName: '', roleKey: '', description: '', sort: 0, status: 1, dataScope: 1, menuIds: [] }
  showModal.value = true
}

function handleEdit(row: RoleVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    roleName: row.roleName,
    roleKey: row.roleKey,
    description: '',
    sort: 0,
    status: 1,
    dataScope: row.dataScope || 1,
    menuIds: []
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true
    if (isEdit.value && formData.value.id) {
      await updateRole(formData.value.id, {
        roleName: formData.value.roleName,
        roleKey: formData.value.roleKey,
        description: formData.value.description,
        sort: formData.value.sort,
        status: formData.value.status
      })
      message.success('更新成功')
    } else {
      await createRole(formData.value)
      message.success('创建成功')
    }
    showModal.value = false
    loadRoles()
  } catch {
    // validation or api error
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteRole(id)
    message.success('删除成功')
    if (selectedRoleId.value === id) {
      selectedRoleId.value = null
      selectedRole.value = null
      roleMenuIds.value.clear()
    }
    loadRoles()
  } catch {
    // interceptor handles error message
  }
}

function handleSearch() {
  loadRoles()
}

function handleReset() {
  queryParams.value = { roleName: '', roleKey: '', status: null }
  loadRoles()
}

// 状态映射
const statusMap: Record<number, { label: string; type: string }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'error' }
}

onMounted(() => {
  loadRoles()
  loadMenuTree()
})
</script>

<template>
  <div class="flex gap-6 h-[calc(100vh-8rem)]">
    <!-- ========== 左侧：角色列表 ========== -->
    <div class="w-64 flex-shrink-0 flex flex-col">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-sm font-semibold" style="color: var(--text-primary)">角色列表</h3>
        <NButton size="tiny" type="primary" @click="handleCreate">新增</NButton>
      </div>

      <!-- 搜索 -->
      <div class="mb-3 space-y-2">
        <NInput
          v-model:value="queryParams.roleName"
          placeholder="搜索角色..."
          clearable
          size="tiny"
          @keyup.enter="handleSearch"
          @clear="handleReset"
        />
      </div>

      <!-- 角色卡片列表 -->
      <div class="flex-1 overflow-y-auto space-y-2">
        <div
          v-for="role in roles"
          :key="role.id"
          class="group px-3 py-2.5 rounded-lg cursor-pointer transition-all duration-200"
          :style="{
            background: selectedRoleId === role.id ? 'var(--accent-soft)' : 'var(--bg-card)',
            border: selectedRoleId === role.id ? '1px solid var(--accent)' : '1px solid var(--border-color)'
          }"
          @click="selectRole(role)"
        >
          <div class="flex items-center justify-between">
            <div class="min-w-0">
              <div class="text-sm font-medium truncate" style="color: var(--text-primary)">
                {{ role.roleName }}
              </div>
              <div class="text-xs truncate" style="color: var(--text-muted)">
                {{ role.roleKey }}
              </div>
            </div>
            <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <NButton v-if="hasPermission('system:role:edit')" text size="tiny" @click.stop="handleEdit(role)">
                <span style="color: var(--text-muted); font-size: 11px">编辑</span>
              </NButton>
              <NPopconfirm v-if="hasPermission('system:role:delete')" @positive-click.stop="handleDelete(role.id)">
                <template #trigger>
                  <NButton text size="tiny" @click.stop>
                    <span style="color: #ef4444; font-size: 11px">删除</span>
                  </NButton>
                </template>
                确认删除？
              </NPopconfirm>
            </div>
          </div>
        </div>

        <NEmpty v-if="!roles.length && !loading" description="暂无角色" class="py-8" />
      </div>
    </div>

    <!-- ========== 右侧：权限配置 ========== -->
    <div class="flex-1 min-w-0 flex flex-col">
      <!-- 未选中提示 -->
      <div v-if="!selectedRole" class="flex-1 flex items-center justify-center">
        <NEmpty description="请从左侧选择一个角色" />
      </div>

      <!-- 选中角色后显示权限表格 -->
      <template v-else>
        <!-- 头部 -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <h3 class="text-sm font-semibold" style="color: var(--text-primary)">
              {{ selectedRole.roleName }}
              <span class="text-xs font-normal ml-2" style="color: var(--text-muted)">
                {{ selectedRole.roleKey }}
              </span>
            </h3>
          </div>
          <div class="flex items-center gap-2">
            <NButton size="tiny" @click="selectAll">全选</NButton>
            <NButton size="tiny" @click="deselectAll">全不选</NButton>
            <NButton size="tiny" type="primary" :loading="saving" @click="handleSavePermissions">
              保存权限
            </NButton>
          </div>
        </div>

        <!-- 权限表格 -->
        <div class="flex-1 overflow-y-auto rounded-lg" style="border: 1px solid var(--border-color)">
          <table class="w-full text-sm" style="border-collapse: collapse">
            <thead>
              <tr style="background: var(--hover-bg)">
                <th
                  v-for="col in tableData"
                  :key="col.dirId"
                  class="px-3 py-2.5 text-left font-semibold border-b"
                  style="color: var(--text-primary); border-color: var(--border-color); min-width: 160px"
                >
                  <div class="flex items-center gap-2">
                    <NCheckbox
                      :checked="isDirChecked(col.dirId, col)"
                      :indeterminate="isDirIndeterminate(col.dirId, col)"
                      @update:checked="toggleDir(col)"
                    />
                    <span>{{ col.dirName }}</span>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <!-- 菜单行 -->
              <tr>
                <td
                  v-for="col in tableData"
                  :key="col.dirId"
                  class="px-3 py-2 align-top border-b"
                  style="border-color: var(--border-color)"
                >
                  <div v-for="menu in col.menus" :key="menu.id" class="mb-3">
                    <!-- 菜单名称 + 勾选 -->
                    <div class="flex items-center gap-2 mb-1.5">
                      <NCheckbox
                        :checked="isChecked(menu.id)"
                        @update:checked="toggleMenu(menu)"
                      />
                      <span class="text-xs font-medium" style="color: var(--text-secondary)">
                        {{ menu.name }}
                      </span>
                    </div>
                    <!-- 按钮权限 -->
                    <div v-if="menu.buttons.length" class="flex flex-wrap gap-x-3 gap-y-1 pl-5">
                      <label
                        v-for="btn in menu.buttons"
                        :key="btn.id"
                        class="flex items-center gap-1 cursor-pointer"
                      >
                        <NCheckbox
                          :checked="isChecked(btn.id)"
                          @update:checked="toggleButton(btn.id)"
                        />
                        <span class="text-xs" style="color: var(--text-muted)">
                          {{ btn.name }}
                        </span>
                      </label>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>

    <!-- ========== 新建/编辑角色弹窗 ========== -->
    <NModal
      v-model:show="showModal"
      :title="isEdit ? '编辑角色' : '新建角色'"
      preset="card"
      style="width: 650px"
    >
      <NForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="70"
      >
        <NFormItem label="名称" path="roleName">
          <NInput v-model:value="formData.roleName" placeholder="角色名称" />
        </NFormItem>
        <NFormItem label="标识" path="roleKey">
          <NInput v-model:value="formData.roleKey" placeholder="角色标识（英文）" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" placeholder="描述" :rows="2" />
        </NFormItem>
        <div class="grid grid-cols-3 gap-3">
          <NFormItem label="排序" path="sort">
            <NInputNumber v-model:value="formData.sort" :min="0" class="w-full" />
          </NFormItem>
          <NFormItem label="状态" path="status">
            <NSelect v-model:value="formData.status" :options="statusOptions" />
          </NFormItem>
          <NFormItem label="数据范围" path="dataScope">
            <NSelect v-model:value="formData.dataScope" :options="dataScopeOptions" />
          </NFormItem>
        </div>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" :loading="saving" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
