import { get, post, put, del } from '@/utils/request'

export interface Dashboard {
  id: string
  name: string
  config: any
  status: number
  shareUrl: string
  remark: string
  createTime: string
  updateTime: string
}

export interface DashboardQuery {
  name?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface DashboardCreate {
  name: string
  config: any
  remark?: string
}

export interface DashboardUpdate {
  name?: string
  config?: any
  remark?: string
}

// 分页查询大屏
export const getDashboardPage = (params: DashboardQuery) => {
  return get<{ records: Dashboard[]; list: Dashboard[]; total: number }>('/report/dashboards', params)
}

// 获取大屏详情
export const getDashboardById = (id: string) => {
  return get<Dashboard>(`/report/dashboards/${id}`)
}

// 创建大屏
export const createDashboard = (data: DashboardCreate) => {
  return post<string>('/report/dashboards', data)
}

// 更新大屏
export const updateDashboard = (id: string, data: DashboardUpdate) => {
  return put<void>(`/report/dashboards/${id}`, data)
}

// 删除大屏
export const deleteDashboard = (id: string) => {
  return del<void>(`/report/dashboards/${id}`)
}

// 分享大屏
export const shareDashboard = (id: string) => {
  return post<string>(`/report/dashboards/${id}/share`)
}

// 获取大屏数据
export const getDashboardData = (id: string) => {
  return get<Record<string, any>>(`/report/dashboards/${id}/data`)
}
