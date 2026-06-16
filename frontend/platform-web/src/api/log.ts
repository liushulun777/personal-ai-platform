import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** 操作日志信息 */
export interface LogVO {
  id: number
  module: string
  operation: string
  method: string
  requestUrl: string
  requestMethod: string
  requestParams: string
  responseData: string
  ip: string
  userAgent: string
  userId: number
  username: string
  status: number
  errorMsg: string
  duration: number
  createTime: string
}

/** 操作日志查询参数 */
export interface LogQueryParams extends PageQuery {
  module?: string
  operation?: string
  status?: number
  username?: string
  startTime?: string
  endTime?: string
}

/** 分页查询操作日志 */
export function getLogPage(params: LogQueryParams) {
  return request.get<ApiResult<PageResult<LogVO>>>('/system/logs', { params })
}

/** 获取操作日志详情 */
export function getLogById(id: number) {
  return request.get<ApiResult<LogVO>>(`/system/logs/${id}`)
}

/** 删除操作日志 */
export function deleteLog(id: number) {
  return request.delete<ApiResult<void>>(`/system/logs/${id}`)
}
