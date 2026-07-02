import { get, post, put, del } from '@/utils/request'

export interface Report {
  id: string
  name: string
  code: string
  type: string
  category: string
  config: any
  status: number
  version: number
  remark: string
  createTime: string
  updateTime: string
}

export interface ReportQuery {
  name?: string
  type?: string
  category?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface ReportCreate {
  name: string
  code: string
  type: string
  category?: string
  config: any
  remark?: string
}

export interface ReportUpdate {
  name?: string
  category?: string
  config?: any
  remark?: string
}

// 分页查询报表
export const getReportPage = (params: ReportQuery) => {
  return get<{ records: Report[]; list: Report[]; total: number }>('/report/reports', params)
}

// 获取报表详情
export const getReportById = (id: string) => {
  return get<Report>(`/report/reports/${id}`)
}

// 创建报表
export const createReport = (data: ReportCreate) => {
  return post<string>('/report/reports', data)
}

// 更新报表
export const updateReport = (id: string, data: ReportUpdate) => {
  return put<void>(`/report/reports/${id}`, data)
}

// 删除报表
export const deleteReport = (id: string) => {
  return del<void>(`/report/reports/${id}`)
}

// 发布报表
export const publishReport = (id: string) => {
  return post<void>(`/report/reports/${id}/publish`)
}

// 归档报表
export const archiveReport = (id: string) => {
  return post<void>(`/report/reports/${id}/archive`)
}

// 获取报表数据
export const getReportData = (id: string, params: Record<string, any>) => {
  return post<Record<string, any>>(`/report/reports/${id}/data`, params)
}

// 导出报表
export const exportReport = (id: string, format: string, params: Record<string, any>) => {
  return post<string>(`/report/reports/${id}/export`, params, { params: { format } })
}
