import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DataSet, DataSetQuery, DataSetField } from '@/api/dataset'
import { getDataSetPage, getDataSetById, createDataSet, updateDataSet, deleteDataSet, previewDataSet, getDataSetFields } from '@/api/dataset'

export const useDataSetStore = defineStore('dataset', () => {
  const list = ref<DataSet[]>([])
  const total = ref(0)
  const current = ref<DataSet | null>(null)
  const fields = ref<DataSetField[]>([])
  const previewData = ref<Record<string, any>[]>([])
  const loading = ref(false)

  const fetchList = async (query: DataSetQuery) => {
    loading.value = true
    try {
      const data = await getDataSetPage(query)
      list.value = data.records || data.list || []
      total.value = data.total || 0
    } finally {
      loading.value = false
    }
  }

  const fetchById = async (id: string) => {
    loading.value = true
    try {
      current.value = await getDataSetById(id)
    } finally {
      loading.value = false
    }
  }

  const create = async (data: any) => {
    return await createDataSet(data)
  }

  const update = async (id: string, data: any) => {
    await updateDataSet(id, data)
  }

  const remove = async (id: string) => {
    await deleteDataSet(id)
  }

  const preview = async (id: string, params: Record<string, any>) => {
    loading.value = true
    try {
      previewData.value = await previewDataSet(id, params)
    } finally {
      loading.value = false
    }
  }

  const fetchFields = async (id: string) => {
    fields.value = await getDataSetFields(id)
  }

  return {
    list,
    total,
    current,
    fields,
    previewData,
    loading,
    fetchList,
    fetchById,
    create,
    update,
    remove,
    preview,
    fetchFields
  }
})
