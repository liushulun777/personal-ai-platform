import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

export function toggleFavorite(articleId: string | number) {
  return request.post<ApiResult<void>>(`/blog/favorites/${articleId}`)
}

export function cancelFavorite(articleId: string | number) {
  return request.delete<ApiResult<void>>(`/blog/favorites/${articleId}`)
}

export function getFavoriteStatus(articleId: string | number) {
  return request.get<ApiResult<boolean>>(`/blog/favorites/${articleId}/status`)
}
