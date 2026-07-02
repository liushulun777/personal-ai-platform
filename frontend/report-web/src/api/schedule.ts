import { get, post, put, del } from '@/utils/request'

export interface Schedule {
  id: string
  name: string
  reportId: string
  reportName?: string
  cron: string
  config: any
  status: number
  lastExecuteTime: string
  nextExecuteTime: string
  remark: string
  createTime: string
}

export interface ScheduleQuery {
  name?: string
  reportId?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface ScheduleCreate {
  name: string
  reportId: string
  cron: string
  config: any
  remark?: string
}

export interface ScheduleUpdate {
  name?: string
  cron?: string
  config?: any
  remark?: string
}

// 分页查询调度任务
export const getSchedulePage = (params: ScheduleQuery) => {
  return get<{ records: Schedule[]; list: Schedule[]; total: number }>('/report/schedules', params)
}

// 获取调度任务详情
export const getScheduleById = (id: string) => {
  return get<Schedule>(`/report/schedules/${id}`)
}

// 创建调度任务
export const createSchedule = (data: ScheduleCreate) => {
  return post<string>('/report/schedules', data)
}

// 更新调度任务
export const updateSchedule = (id: string, data: ScheduleUpdate) => {
  return put<void>(`/report/schedules/${id}`, data)
}

// 删除调度任务
export const deleteSchedule = (id: string) => {
  return del<void>(`/report/schedules/${id}`)
}

// 启动调度任务
export const startSchedule = (id: string) => {
  return post<void>(`/report/schedules/${id}/start`)
}

// 停止调度任务
export const stopSchedule = (id: string) => {
  return post<void>(`/report/schedules/${id}/stop`)
}

// 暂停调度任务
export const pauseSchedule = (id: string) => {
  return post<void>(`/report/schedules/${id}/pause`)
}
