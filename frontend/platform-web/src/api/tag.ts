import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 标签信息 */
export interface TagVO {
  id: number
  name: string
  slug: string
  description: string
  color: string
  sort: number
  status: number
  createTime: string
}

/** 创建标签参数 */
export interface TagCreateParams {
  name: string
  slug: string
  description?: string
  color?: string
  sort?: number
  status?: number
}

/** 更新标签参数 */
export interface TagUpdateParams {
  name: string
  slug: string
  description?: string
  color?: string
  sort?: number
  status?: number
}

/**
 * 获取所有标签列表
 */
export function getAllTags() {
  return request.get<ApiResult<TagVO[]>>('/blog/tags')
}

/**
 * 获取标签详情
 */
export function getTagById(id: number) {
  return request.get<ApiResult<TagVO>>(`/blog/tags/${id}`)
}

/**
 * 创建标签
 */
export function createTag(data: TagCreateParams) {
  return request.post<ApiResult<number>>('/blog/tags', data)
}

/**
 * 更新标签
 */
export function updateTag(id: number, data: TagUpdateParams) {
  return request.put<ApiResult<void>>(`/blog/tags/${id}`, data)
}

/**
 * 删除标签
 */
export function deleteTag(id: number) {
  return request.delete<ApiResult<void>>(`/blog/tags/${id}`)
}
