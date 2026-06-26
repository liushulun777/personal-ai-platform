import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'
import type { TagVO } from './tag'

/** 文章信息 */
export interface ArticleVO {
  id: number
  title: string
  summary: string
  cover: string
  authorId: number
  authorName: string
  categoryId: number
  categoryName: string
  status: number
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  isTop: number
  isOriginal: number
  publishTime: string
  createTime: string
  tags: TagVO[]
}

/** 文章详情 */
export interface ArticleDetailVO extends ArticleVO {
  content: string
  sourceUrl: string
  tagIds: number[]
}

/** 文章查询参数 */
export interface ArticleQueryParams extends PageQuery {
  title?: string
  categoryId?: number
  tagId?: number
  status?: number
  authorId?: number
  applyDataScope?: boolean
}

/** 创建文章参数 */
export interface ArticleCreateParams {
  title: string
  summary?: string
  content?: string
  cover?: string
  categoryId?: number
  tagIds?: number[]
  isTop?: number
  isOriginal?: number
  sourceUrl?: string
}

/** 更新文章参数 */
export interface ArticleUpdateParams {
  title: string
  summary?: string
  content?: string
  cover?: string
  categoryId?: number
  tagIds?: number[]
  isTop?: number
  isOriginal?: number
  sourceUrl?: string
}

/**
 * 分页查询文章
 */
export function getArticlePage(params: ArticleQueryParams) {
  return request.get<ApiResult<PageResult<ArticleVO>>>('/blog/articles', { params })
}

/**
 * 获取文章详情
 */
export function getArticleById(id: string | number) {
  return request.get<ApiResult<ArticleDetailVO>>(`/blog/articles/${id}`)
}

/**
 * 创建文章
 */
export function createArticle(data: ArticleCreateParams) {
  return request.post<ApiResult<string>>('/blog/articles', data)
}

/**
 * 更新文章
 */
export function updateArticle(id: string | number, data: ArticleUpdateParams) {
  return request.put<ApiResult<void>>(`/blog/articles/${id}`, data)
}

/**
 * 删除文章
 */
export function deleteArticle(id: string | number) {
  return request.delete<ApiResult<void>>(`/blog/articles/${id}`)
}

/**
 * 发布文章
 */
export function publishArticle(id: string | number) {
  return request.put<ApiResult<void>>(`/blog/articles/${id}/publish`)
}

/**
 * 归档文章
 */
export function archiveArticle(id: string | number) {
  return request.put<ApiResult<void>>(`/blog/articles/${id}/archive`)
}
