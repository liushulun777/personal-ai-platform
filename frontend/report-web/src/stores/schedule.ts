import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Schedule, ScheduleQuery } from '@/api/schedule'
import { getSchedulePage, getScheduleById, createSchedule, updateSchedule, deleteSchedule, startSchedule, stopSchedule, pauseSchedule } from '@/api/schedule'

export const useScheduleStore = defineStore('schedule', () => {
  const list = ref<Schedule[]>([])
  const total = ref(0)
  const current = ref<Schedule | null>(null)
  const loading = ref(false)

  const fetchList = async (query: ScheduleQuery) => {
    loading.value = true
    try {
      const data = await getSchedulePage(query)
      list.value = data.records || data.list || []
      total.value = data.total || 0
    } finally {
      loading.value = false
    }
  }

  const fetchById = async (id: string) => {
    loading.value = true
    try {
      current.value = await getScheduleById(id)
    } finally {
      loading.value = false
    }
  }

  const create = async (data: any) => {
    return await createSchedule(data)
  }

  const update = async (id: string, data: any) => {
    await updateSchedule(id, data)
  }

  const remove = async (id: string) => {
    await deleteSchedule(id)
  }

  const start = async (id: string) => {
    await startSchedule(id)
  }

  const stop = async (id: string) => {
    await stopSchedule(id)
  }

  const pause = async (id: string) => {
    await pauseSchedule(id)
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
    start,
    stop,
    pause
  }
})
