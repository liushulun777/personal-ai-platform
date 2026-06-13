import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 点赞目标类型: 1-文章, 2-评论 */
export type LikeTargetType = 1 | 2

export function toggleLike(targetId: string | number, targetType: LikeTargetType = 1) {
  return request.post<ApiResult<void>>(`/blog/likes/${targetId}`, null, { params: { targetType } })
}

export function cancelLike(targetId: string | number, targetType: LikeTargetType = 1) {
  return request.delete<ApiResult<void>>(`/blog/likes/${targetId}`, { params: { targetType } })
}

export function getLikeStatus(targetId: string | number, targetType: LikeTargetType = 1) {
  return request.get<ApiResult<boolean>>(`/blog/likes/${targetId}/status`, { params: { targetType } })
}
