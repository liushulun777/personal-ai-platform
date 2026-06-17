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
  NSelect,
  NTreeSelect,
  NPopconfirm,
  useMessage
} from 'naive-ui'
import { NTag } from 'naive-ui/es/tag'
import type { DataTableColumns, FormInst, TreeSelectOption, SelectOption } from 'naive-ui'
import {
  getMenuTree,
  createMenu,
  updateMenu,
  deleteMenu
} from '@/api/menu'
import type { MenuVO, MenuCreateParams, MenuUpdateParams } from '@/api/menu'

const message = useMessage()
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

// 菜单列表
const menuList = ref<MenuVO[]>([])

// 菜单树（用于父菜单选择）
const menuTreeOptions = ref<TreeSelectOption[]>([])

// 表单数据
const formData = ref<MenuCreateParams & { id?: number }>({
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  icon: '',
  menuType: 0,
  permission: '',
  sort: 0,
  visible: 1,
  status: 1
})

// 表单验证规则
const formRules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ]
}

// 菜单类型选项
const menuTypeOptions: SelectOption[] = [
  { label: '目录', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 }
]

// 菜单类型映射
const menuTypeMap: Record<number, { label: string; type: 'default' | 'info' | 'warning' }> = {
  0: { label: '目录', type: 'default' },
  1: { label: '菜单', type: 'info' },
  2: { label: '按钮', type: 'warning' }
}

// 状态选项
const statusOptions: SelectOption[] = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

// 可见性选项
const visibleOptions: SelectOption[] = [
  { label: '可见', value: 1 },
  { label: '隐藏', value: 0 }
]

// 表格列定义
const columns: DataTableColumns<MenuVO> = [
  { title: '菜单名称', key: 'menuName', width: 200 },
  {
    title: '类型',
    key: 'menuType',
    width: 80,
    render(row) {
      const type = menuTypeMap[row.menuType]
      return h(NTag, { type: type.type, size: 'small' }, { default: () => type.label })
    }
  },
  { title: '路由路径', key: 'path', width: 150 },
  { title: '组件路径', key: 'component', width: 200 },
  { title: '权限标识', key: 'permission', width: 150 },
  { title: '排序', key: 'sort', width: 80 },
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
            onClick: () => handleAddChild(row.id)
          }, { default: () => '添加子菜单' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该菜单？'
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
  loading.value = true
  try {
    const { data } = await getMenuTree()
    menuList.value = data.data
    menuTreeOptions.value = [
      { key: 0, label: '根目录' },
      ...convertMenuTree(data.data)
    ]
  } catch (error) {
    message.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 新建菜单
function handleCreate() {
  isEdit.value = false
  formData.value = {
    parentId: 0,
    menuName: '',
    path: '',
    component: '',
    icon: '',
    menuType: 0,
    permission: '',
    sort: 0,
    visible: 1,
    status: 1
  }
  showModal.value = true
}

// 添加子菜单
function handleAddChild(parentId: number) {
  isEdit.value = false
  formData.value = {
    parentId,
    menuName: '',
    path: '',
    component: '',
    icon: '',
    menuType: 1,
    permission: '',
    sort: 0,
    visible: 1,
    status: 1
  }
  showModal.value = true
}

// 编辑菜单
function handleEdit(row: MenuVO) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    parentId: row.parentId,
    menuName: row.menuName,
    path: row.path,
    component: row.component,
    icon: row.icon,
    menuType: row.menuType,
    permission: row.permission,
    sort: row.sort,
    visible: row.visible,
    status: row.status
  }
  showModal.value = true
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()

    if (isEdit.value && formData.value.id) {
      const updateData: MenuUpdateParams = {
        parentId: formData.value.parentId,
        menuName: formData.value.menuName,
        path: formData.value.path,
        component: formData.value.component,
        icon: formData.value.icon,
        menuType: formData.value.menuType,
        permission: formData.value.permission,
        sort: formData.value.sort,
        visible: formData.value.visible,
        status: formData.value.status
      }
      await updateMenu(formData.value.id, updateData)
      message.success('更新成功')
    } else {
      await createMenu(formData.value)
      message.success('创建成功')
    }

    showModal.value = false
    loadMenuTree()
  } catch (error) {
    // 表单验证失败或接口错误
  }
}

// 删除菜单
async function handleDelete(id: number) {
  try {
    await deleteMenu(id)
    message.success('删除成功')
    loadMenuTree()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadMenuTree()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">菜单管理</h2>
      <NButton type="primary" size="small" @click="handleCreate">
        新建菜单
      </NButton>
    </div>

    <!-- 菜单列表 -->
    <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
      <NDataTable
        :columns="columns"
        :data="menuList"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row: MenuVO) => row.id"
        default-expand-all
      />
    </div>

    <!-- 新建/编辑弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="isEdit ? '编辑菜单' : '新建菜单'"
      preset="card"
      style="width: 600px"
    >
      <NForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80"
      >
        <NFormItem label="父菜单" path="parentId">
          <NTreeSelect
            v-model:value="formData.parentId"
            :options="menuTreeOptions"
            placeholder="请选择父菜单"
          />
        </NFormItem>
        <NFormItem label="菜单类型" path="menuType">
          <NSelect
            v-model:value="formData.menuType"
            :options="menuTypeOptions"
          />
        </NFormItem>
        <NFormItem label="菜单名称" path="menuName">
          <NInput
            v-model:value="formData.menuName"
            placeholder="请输入菜单名称"
          />
        </NFormItem>
        <NFormItem v-if="formData.menuType !== 2" label="路由路径" path="path">
          <NInput
            v-model:value="formData.path"
            placeholder="请输入路由路径"
          />
        </NFormItem>
        <NFormItem v-if="formData.menuType === 1" label="组件路径" path="component">
          <NInput
            v-model:value="formData.component"
            placeholder="请输入组件路径"
          />
        </NFormItem>
        <NFormItem v-if="formData.menuType !== 2" label="图标" path="icon">
          <NInput
            v-model:value="formData.icon"
            placeholder="请输入图标"
          />
        </NFormItem>
        <NFormItem v-if="formData.menuType === 2" label="权限标识" path="permission">
          <NInput
            v-model:value="formData.permission"
            placeholder="请输入权限标识"
          />
        </NFormItem>
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" />
        </NFormItem>
        <NFormItem v-if="formData.menuType !== 2" label="可见性" path="visible">
          <NSelect
            v-model:value="formData.visible"
            :options="visibleOptions"
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
