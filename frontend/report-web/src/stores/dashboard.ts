import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Dashboard, DashboardQuery } from '@/api/dashboard'
import { getDashboardPage, getDashboardById, createDashboard, updateDashboard, deleteDashboard, shareDashboard, getDashboardData } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  const list = ref<Dashboard[]>([])
  const total = ref(0)
  const current = ref<Dashboard | null>(null)
  const dashboardData = ref<Record<string, any>>({})
  const loading = ref(false)

  const fetchList = async (query: DashboardQuery) => {
    loading.value = true
    try {
      const data = await getDashboardPage(query)
      list.value = data.records || data.list || []
      total.value = data.total || 0
    } finally {
      loading.value = false
    }
  }

  const fetchById = async (id: string) => {
    loading.value = true
    try {
      current.value = await getDashboardById(id)
    } finally {
      loading.value = false
    }
  }

  const create = async (data: any) => {
    return await createDashboard(data)
  }

  const update = async (id: string, data: any) => {
    await updateDashboard(id, data)
  }

  const remove = async (id: string) => {
    await deleteDashboard(id)
  }

  const share = async (id: string) => {
    return await shareDashboard(id)
  }

  const fetchData = async (id: string) => {
    loading.value = true
    try {
      dashboardData.value = await getDashboardData(id)
    } finally {
      loading.value = false
    }
  }

  return {
    list,
    total,
    current,
    dashboardData,
    loading,
    fetchList,
    fetchById,
    create,
    update,
    remove,
    share,
    fetchData
  }
})
