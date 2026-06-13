import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

export interface BlogStatisticsVO {
  articleCount: number
  categoryCount: number
  tagCount: number
  commentCount: number
  totalViews: number
  totalLikes: number
}

export function getBlogStatistics() {
  return request.get<ApiResult<BlogStatisticsVO>>('/blog/statistics')
}
