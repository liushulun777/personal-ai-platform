import { get, post, put, del } from '@/utils/request'

export interface DataSet {
  id: string
  name: string
  type: string
  sourceId: string
  sourceName?: string
  config: any
  fields: any[]
  status: number
  remark: string
  createTime: string
  updateTime: string
}

export interface DataSetQuery {
  name?: string
  type?: string
  sourceId?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface DataSetCreate {
  name: string
  type: string
  sourceId: string
  config: any
  remark?: string
}

export interface DataSetUpdate {
  name?: string
  config?: any
  status?: number
  remark?: string
}

export interface DataSetField {
  name: string
  label: string
  type: string
}

// 分页查询数据集
export const getDataSetPage = (params: DataSetQuery) => {
  return get<{ records: DataSet[]; list: DataSet[]; total: number }>('/report/datasets', params)
}

// 获取数据集详情
export const getDataSetById = (id: string) => {
  return get<DataSet>(`/report/datasets/${id}`)
}

// 创建数据集
export const createDataSet = (data: DataSetCreate) => {
  return post<string>('/report/datasets', data)
}

// 更新数据集
export const updateDataSet = (id: string, data: DataSetUpdate) => {
  return put<void>(`/report/datasets/${id}`, data)
}

// 删除数据集
export const deleteDataSet = (id: string) => {
  return del<void>(`/report/datasets/${id}`)
}

// 预览数据集数据
export const previewDataSet = (id: string, params: Record<string, any>) => {
  return post<Record<string, any>[]>(`/report/datasets/${id}/preview`, params)
}

// 获取数据集字段
export const getDataSetFields = (id: string) => {
  return get<DataSetField[]>(`/report/datasets/${id}/fields`)
}
