import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 分类信息 */
export interface CategoryVO {
  id: number
  name: string
  slug: string
  description: string
  sort: number
  status: number
  createTime: string
}

/** 创建分类参数 */
export interface CategoryCreateParams {
  name: string
  slug: string
  description?: string
  sort?: number
  status?: number
}

/** 更新分类参数 */
export interface CategoryUpdateParams {
  name: string
  slug: string
  description?: string
  sort?: number
  status?: number
}

/**
 * 获取所有分类列表
 */
export function getAllCategories() {
  return request.get<ApiResult<CategoryVO[]>>('/blog/categories')
}

/**
 * 获取分类详情
 */
export function getCategoryById(id: number) {
  return request.get<ApiResult<CategoryVO>>(`/blog/categories/${id}`)
}

/**
 * 创建分类
 */
export function createCategory(data: CategoryCreateParams) {
  return request.post<ApiResult<number>>('/blog/categories', data)
}

/**
 * 更新分类
 */
export function updateCategory(id: number, data: CategoryUpdateParams) {
  return request.put<ApiResult<void>>(`/blog/categories/${id}`, data)
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return request.delete<ApiResult<void>>(`/blog/categories/${id}`)
}
