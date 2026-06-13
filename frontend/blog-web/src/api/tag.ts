import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

export interface TagVO {
  id: number
  name: string
  slug: string
  description: string
  color: string
  sort: number
  status: number
}

export function getAllTags() {
  return request.get<ApiResult<TagVO[]>>('/blog/tags')
}
