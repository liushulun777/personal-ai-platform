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
  NTabs,
  NTabPane,
  NPopconfirm,
  useMessage
} from 'naive-ui'
import { NTag } from 'naive-ui/es/tag'
import type { DataTableColumns, FormInst } from 'naive-ui'
import {
  getDictTypePage,
  createDictType,
  updateDictType,
  deleteDictType,
  getDictDataPage,
  createDictData,
  updateDictData,
  deleteDictData
} from '@/api/dict'
import type {
  DictTypeVO,
  DictTypeCreateParams,
  DictDataVO,
  DictDataCreateParams
} from '@/api/dict'
import { usePermission } from '@/composables/usePermission'

const message = useMessage()
const { hasPermission } = usePermission()

// ========== 字典类型 ==========
const typeLoading = ref(false)
const showTypeModal = ref(false)
const isEditType = ref(false)
const typeFormRef = ref<FormInst | null>(null)

const typeQueryParams = ref({
  dictName: '',
  dictType: ''
})

const dictTypes = ref<DictTypeVO[]>([])
const typePagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const typeFormData = ref<DictTypeCreateParams & { id?: number }>({
  dictName: '',
  dictType: '',
  description: '',
  status: 1
})

const typeFormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const statusMap: Record<number, { label: string; type: 'success' | 'error' }> = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'error' }
}

const typeColumns: DataTableColumns<DictTypeVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '字典名称', key: 'dictName', width: 150 },
  { title: '字典类型', key: 'dictType', width: 200 },
  { title: '描述', key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const s = statusMap[row.status]
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '未知' })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      const buttons: any[] = []
      if (hasPermission('system:dict:edit')) buttons.push(h(NButton, { size: 'small', onClick: () => handleEditType(row) }, { default: () => '编辑' }))
      if (hasPermission('system:dict:delete')) buttons.push(h(NPopconfirm, { onPositiveClick: () => handleDeleteType(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除该字典类型？'
      }))
      return h(NSpace, { size: 'small' }, { default: () => buttons })
    }
  }
]

async function loadDictTypes() {
  typeLoading.value = true
  try {
    const { data } = await getDictTypePage({
      current: typePagination.value.page,
      size: typePagination.value.pageSize,
      dictName: typeQueryParams.value.dictName || undefined,
      dictType: typeQueryParams.value.dictType || undefined
    })
    dictTypes.value = data.data.records
    typePagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载字典类型列表失败')
  } finally {
    typeLoading.value = false
  }
}

function handleTypeSearch() {
  typePagination.value.page = 1
  loadDictTypes()
}

function handleTypeReset() {
  typeQueryParams.value = { dictName: '', dictType: '' }
  handleTypeSearch()
}

function handleCreateType() {
  isEditType.value = false
  typeFormData.value = { dictName: '', dictType: '', description: '', status: 1 }
  showTypeModal.value = true
}

async function handleEditType(row: DictTypeVO) {
  isEditType.value = true
  typeFormData.value = {
    id: row.id,
    dictName: row.dictName,
    dictType: row.dictType,
    description: row.description,
    status: row.status
  }
  showTypeModal.value = true
}

async function handleTypeSubmit() {
  try {
    await typeFormRef.value?.validate()
    if (isEditType.value && typeFormData.value.id) {
      await updateDictType(typeFormData.value.id, {
        dictName: typeFormData.value.dictName,
        dictType: typeFormData.value.dictType,
        description: typeFormData.value.description,
        status: typeFormData.value.status
      })
      message.success('更新成功')
    } else {
      await createDictType(typeFormData.value)
      message.success('创建成功')
    }
    showTypeModal.value = false
    loadDictTypes()
  } catch (error) {
    // validation or api error
  }
}

async function handleDeleteType(id: number) {
  try {
    await deleteDictType(id)
    message.success('删除成功')
    loadDictTypes()
  } catch (error) {
    // interceptor handles error message
  }
}

function handleTypePageChange(page: number) {
  typePagination.value.page = page
  loadDictTypes()
}

function handleTypePageSizeChange(pageSize: number) {
  typePagination.value.pageSize = pageSize
  typePagination.value.page = 1
  loadDictTypes()
}

// ========== 字典数据 ==========
const dataLoading = ref(false)
const showDataModal = ref(false)
const isEditData = ref(false)
const dataFormRef = ref<FormInst | null>(null)
const selectedDictType = ref('')

const dataQueryParams = ref({
  dictLabel: ''
})

const dictDataList = ref<DictDataVO[]>([])
const dataPagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const dataFormData = ref<DictDataCreateParams & { id?: number }>({
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
  remark: ''
})

const dataFormRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const dataColumns: DataTableColumns<DictDataVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '字典标签', key: 'dictLabel', width: 150 },
  { title: '字典值', key: 'dictValue', width: 150 },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const s = statusMap[row.status]
      return h(NTag, { type: s?.type, size: 'small' }, { default: () => s?.label || '未知' })
    }
  },
  { title: '备注', key: 'remark', width: 200, ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      const buttons: any[] = []
      if (hasPermission('system:dict:edit')) buttons.push(h(NButton, { size: 'small', onClick: () => handleEditData(row) }, { default: () => '编辑' }))
      if (hasPermission('system:dict:delete')) buttons.push(h(NPopconfirm, { onPositiveClick: () => handleDeleteData(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
        default: () => '确认删除该字典数据？'
      }))
      return h(NSpace, { size: 'small' }, { default: () => buttons })
    }
  }
]

async function loadDictData() {
  if (!selectedDictType.value) return
  dataLoading.value = true
  try {
    const { data } = await getDictDataPage({
      current: dataPagination.value.page,
      size: dataPagination.value.pageSize,
      dictType: selectedDictType.value,
      dictLabel: dataQueryParams.value.dictLabel || undefined
    })
    dictDataList.value = data.data.records
    dataPagination.value.itemCount = data.data.total
  } catch (error) {
    message.error('加载字典数据列表失败')
  } finally {
    dataLoading.value = false
  }
}

function handleDataSearch() {
  dataPagination.value.page = 1
  loadDictData()
}

function handleDataReset() {
  dataQueryParams.value = { dictLabel: '' }
  handleDataSearch()
}

function handleCreateData() {
  isEditData.value = false
  dataFormData.value = {
    dictType: selectedDictType.value,
    dictLabel: '',
    dictValue: '',
    sort: 0,
    status: 1,
    remark: ''
  }
  showDataModal.value = true
}

function handleEditData(row: DictDataVO) {
  isEditData.value = true
  dataFormData.value = {
    id: row.id,
    dictType: row.dictType,
    dictLabel: row.dictLabel,
    dictValue: row.dictValue,
    sort: row.sort,
    status: row.status,
    remark: row.remark
  }
  showDataModal.value = true
}

async function handleDataSubmit() {
  try {
    await dataFormRef.value?.validate()
    if (isEditData.value && dataFormData.value.id) {
      await updateDictData(dataFormData.value.id, {
        dictType: dataFormData.value.dictType,
        dictLabel: dataFormData.value.dictLabel,
        dictValue: dataFormData.value.dictValue,
        sort: dataFormData.value.sort,
        status: dataFormData.value.status,
        remark: dataFormData.value.remark
      })
      message.success('更新成功')
    } else {
      await createDictData(dataFormData.value)
      message.success('创建成功')
    }
    showDataModal.value = false
    loadDictData()
  } catch (error) {
    // validation or api error
  }
}

async function handleDeleteData(id: number) {
  try {
    await deleteDictData(id)
    message.success('删除成功')
    loadDictData()
  } catch (error) {
    // interceptor handles error message
  }
}

function handleDataPageChange(page: number) {
  dataPagination.value.page = page
  loadDictData()
}

function handleDataPageSizeChange(pageSize: number) {
  dataPagination.value.pageSize = pageSize
  dataPagination.value.page = 1
  loadDictData()
}

// 当选中字典类型时加载字典数据
function handleSelectDictType(dictType: string) {
  selectedDictType.value = dictType
  dataPagination.value.page = 1
  loadDictData()
}

onMounted(() => {
  loadDictTypes()
})
</script>

<template>
  <div>
    <h2 class="text-lg font-semibold mb-6" style="color: var(--text-primary)">字典管理</h2>

    <NTabs type="line" animated>
      <!-- 字典类型 Tab -->
      <NTabPane name="type" tab="字典类型">
        <div class="flex justify-between items-center mb-4">
          <div />
          <NButton v-if="hasPermission('system:dict:add')" type="primary" size="small" @click="handleCreateType">
            新建字典类型
          </NButton>
        </div>

        <div class="flex items-center gap-3 mb-4">
          <NInput
            v-model:value="typeQueryParams.dictName"
            placeholder="字典名称"
            clearable
            size="small"
            class="max-w-xs"
            @keyup.enter="handleTypeSearch"
          />
          <NInput
            v-model:value="typeQueryParams.dictType"
            placeholder="字典类型"
            clearable
            size="small"
            class="max-w-xs"
            @keyup.enter="handleTypeSearch"
          />
          <NButton size="small" type="primary" @click="handleTypeSearch">搜索</NButton>
          <NButton size="small" @click="handleTypeReset">重置</NButton>
        </div>

        <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
          <NDataTable
            :columns="typeColumns"
            :data="dictTypes"
            :loading="typeLoading"
            :bordered="false"
            :single-line="false"
            :pagination="typePagination"
            @update:page="handleTypePageChange"
            @update:page-size="handleTypePageSizeChange"
          />
        </div>

        <!-- 字典类型弹窗 -->
        <NModal
          v-model:show="showTypeModal"
          :title="isEditType ? '编辑字典类型' : '新建字典类型'"
          preset="card"
          style="width: 500px"
        >
          <NForm
            ref="typeFormRef"
            :model="typeFormData"
            :rules="typeFormRules"
            label-placement="left"
            label-width="80"
          >
            <NFormItem label="字典名称" path="dictName">
              <NInput v-model:value="typeFormData.dictName" placeholder="请输入字典名称" />
            </NFormItem>
            <NFormItem label="字典类型" path="dictType">
              <NInput v-model:value="typeFormData.dictType" placeholder="请输入字典类型" :disabled="isEditType" />
            </NFormItem>
            <NFormItem label="描述" path="description">
              <NInput v-model:value="typeFormData.description" type="textarea" placeholder="请输入描述" />
            </NFormItem>
            <NFormItem label="状态" path="status">
              <NSelect v-model:value="typeFormData.status" :options="statusOptions" />
            </NFormItem>
          </NForm>
          <template #footer>
            <NSpace justify="end">
              <NButton @click="showTypeModal = false">取消</NButton>
              <NButton type="primary" @click="handleTypeSubmit">确定</NButton>
            </NSpace>
          </template>
        </NModal>
      </NTabPane>

      <!-- 字典数据 Tab -->
      <NTabPane name="data" tab="字典数据">
        <div class="flex items-center gap-3 mb-4">
          <NSelect
            v-model:value="selectedDictType"
            placeholder="请选择字典类型"
            clearable
            size="small"
            class="w-64"
            :options="dictTypes.map(t => ({ label: `${t.dictName} (${t.dictType})`, value: t.dictType }))"
            @update:value="handleSelectDictType"
          />
          <NButton type="primary" size="small" :disabled="!selectedDictType" @click="handleCreateData">
            新建字典数据
          </NButton>
        </div>

        <div v-if="selectedDictType" class="flex items-center gap-3 mb-4">
          <NInput
            v-model:value="dataQueryParams.dictLabel"
            placeholder="字典标签"
            clearable
            size="small"
            class="max-w-xs"
            @keyup.enter="handleDataSearch"
          />
          <NButton size="small" type="primary" @click="handleDataSearch">搜索</NButton>
          <NButton size="small" @click="handleDataReset">重置</NButton>
        </div>

        <div class="border border-[var(--border-color)] rounded-lg overflow-hidden">
          <NDataTable
            :columns="dataColumns"
            :data="dictDataList"
            :loading="dataLoading"
            :bordered="false"
            :single-line="false"
            :pagination="dataPagination"
            @update:page="handleDataPageChange"
            @update:page-size="handleDataPageSizeChange"
          />
        </div>

        <!-- 字典数据弹窗 -->
        <NModal
          v-model:show="showDataModal"
          :title="isEditData ? '编辑字典数据' : '新建字典数据'"
          preset="card"
          style="width: 500px"
        >
          <NForm
            ref="dataFormRef"
            :model="dataFormData"
            :rules="dataFormRules"
            label-placement="left"
            label-width="80"
          >
            <NFormItem label="字典类型" path="dictType">
              <NInput v-model:value="dataFormData.dictType" placeholder="请输入字典类型" :disabled="isEditData" />
            </NFormItem>
            <NFormItem label="字典标签" path="dictLabel">
              <NInput v-model:value="dataFormData.dictLabel" placeholder="请输入字典标签" />
            </NFormItem>
            <NFormItem label="字典值" path="dictValue">
              <NInput v-model:value="dataFormData.dictValue" placeholder="请输入字典值" />
            </NFormItem>
            <NFormItem label="排序" path="sort">
              <NInputNumber v-model:value="dataFormData.sort" :min="0" />
            </NFormItem>
            <NFormItem label="状态" path="status">
              <NSelect v-model:value="dataFormData.status" :options="statusOptions" />
            </NFormItem>
            <NFormItem label="备注" path="remark">
              <NInput v-model:value="dataFormData.remark" type="textarea" placeholder="请输入备注" />
            </NFormItem>
          </NForm>
          <template #footer>
            <NSpace justify="end">
              <NButton @click="showDataModal = false">取消</NButton>
              <NButton type="primary" @click="handleDataSubmit">确定</NButton>
            </NSpace>
          </template>
        </NModal>
      </NTabPane>
    </NTabs>
  </div>
</template>
