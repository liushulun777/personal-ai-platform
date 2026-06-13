/** 统一响应结构 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页响应结构 */
export interface PageResult<T = unknown> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 分页查询参数 */
export interface PageQuery {
  current?: number
  size?: number
}
