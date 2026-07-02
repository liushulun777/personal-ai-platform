import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DataSource, DataSourceQuery } from '@/api/datasource'
import { getDataSourcePage, getDataSourceById, createDataSource, updateDataSource, deleteDataSource, testDataSourceConnection } from '@/api/datasource'

export const useDataSourceStore = defineStore('datasource', () => {
  const list = ref<DataSource[]>([])
  const total = ref(0)
  const current = ref<DataSource | null>(null)
  const loading = ref(false)

  const fetchList = async (query: DataSourceQuery) => {
    loading.value = true
    try {
      const data = await getDataSourcePage(query)
      list.value = data.records || data.list || []
      total.value = data.total || 0
    } finally {
      loading.value = false
    }
  }

  const fetchById = async (id: string) => {
    loading.value = true
    try {
      current.value = await getDataSourceById(id)
    } finally {
      loading.value = false
    }
  }

  const create = async (data: any) => {
    return await createDataSource(data)
  }

  const update = async (id: string, data: any) => {
    await updateDataSource(id, data)
  }

  const remove = async (id: string) => {
    await deleteDataSource(id)
  }

  const testConnection = async (id: string) => {
    return await testDataSourceConnection(id)
  }

  return {
    list,
    total,
    current,
    loading,
    fetchList,
    fetchById,
    create,
    update,
    remove,
    testConnection
  }
})
