import { get, post, put, del } from '@/utils/request'

export interface DataSource {
  id: string
  name: string
  type: string
  config: any
  status: number
  remark: string
  createTime: string
  updateTime: string
}

export interface DataSourceQuery {
  name?: string
  type?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface DataSourceCreate {
  name: string
  type: string
  config: any
  remark?: string
}

export interface DataSourceUpdate {
  name?: string
  config?: any
  status?: number
  remark?: string
}

// 分页查询数据源
export const getDataSourcePage = (params: DataSourceQuery) => {
  return get<{ records: DataSource[]; list: DataSource[]; total: number }>('/report/datasources', params)
}

// 获取数据源详情
export const getDataSourceById = (id: string) => {
  return get<DataSource>(`/report/datasources/${id}`)
}

// 创建数据源
export const createDataSource = (data: DataSourceCreate) => {
  return post<string>('/report/datasources', data)
}

// 更新数据源
export const updateDataSource = (id: string, data: DataSourceUpdate) => {
  return put<void>(`/report/datasources/${id}`, data)
}

// 删除数据源
export const deleteDataSource = (id: string) => {
  return del<void>(`/report/datasources/${id}`)
}

// 测试数据源连接
export const testDataSourceConnection = (id: string) => {
  return post<boolean>(`/report/datasources/${id}/test`)
}
