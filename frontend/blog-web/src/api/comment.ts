import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

export interface CommentVO {
  id: number
  articleId: number
  parentId: number | null
  userId: number
  username: string
  nickname: string
  avatar: string
  content: string
  likeCount: number
  createTime: string
  children?: CommentVO[]
}

export interface CommentCreateParams {
  articleId: number
  parentId?: number
  content: string
}

export function getCommentsByArticleId(articleId: string | number) {
  return request.get<ApiResult<CommentVO[]>>(`/blog/comments/article/${articleId}`)
}

export function createComment(data: CommentCreateParams) {
  return request.post<ApiResult<number>>('/blog/comments', data)
}

export function deleteComment(id: number) {
  return request.delete<ApiResult<void>>(`/blog/comments/${id}`)
}
