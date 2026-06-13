import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

export interface CategoryVO {
  id: number
  name: string
  slug: string
  description: string
  sort: number
  status: number
}

export function getAllCategories() {
  return request.get<ApiResult<CategoryVO[]>>('/blog/categories')
}
