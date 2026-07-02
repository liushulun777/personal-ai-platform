import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Report, ReportQuery } from '@/api/report'
import { getReportPage, getReportById, createReport, updateReport, deleteReport, publishReport, archiveReport, getReportData } from '@/api/report'

export const useReportStore = defineStore('report', () => {
  const list = ref<Report[]>([])
  const total = ref(0)
  const current = ref<Report | null>(null)
  const reportData = ref<Record<string, any>>({})
  const loading = ref(false)

  const fetchList = async (query: ReportQuery) => {
    loading.value = true
    try {
      const data = await getReportPage(query)
      list.value = data.records || data.list || []
      total.value = data.total || 0
    } finally {
      loading.value = false
    }
  }

  const fetchById = async (id: string) => {
    loading.value = true
    try {
      current.value = await getReportById(id)
    } finally {
      loading.value = false
    }
  }

  const create = async (data: any) => {
    return await createReport(data)
  }

  const update = async (id: string, data: any) => {
    await updateReport(id, data)
  }

  const remove = async (id: string) => {
    await deleteReport(id)
  }

  const publish = async (id: string) => {
    await publishReport(id)
  }

  const archive = async (id: string) => {
    await archiveReport(id)
  }

  const fetchData = async (id: string, params: Record<string, any>) => {
    loading.value = true
    try {
      reportData.value = await getReportData(id, params)
    } finally {
      loading.value = false
    }
  }

  return {
    list,
    total,
    current,
    reportData,
    loading,
    fetchList,
    fetchById,
    create,
    update,
    remove,
    publish,
    archive,
    fetchData
  }
})
