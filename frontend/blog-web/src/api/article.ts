import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

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

export interface ArticleDetailVO extends ArticleVO {
  content: string
  sourceUrl: string
  tagIds: number[]
}

export interface TagVO {
  id: number
  name: string
  slug: string
  color: string
}

export interface ArticleQueryParams extends PageQuery {
  title?: string
  categoryId?: number
  status?: number
}

export function getArticlePage(params: ArticleQueryParams) {
  return request.get<ApiResult<PageResult<ArticleVO>>>('/blog/articles', { params })
}

export function getArticleById(id: string | number) {
  return request.get<ApiResult<ArticleDetailVO>>(`/blog/articles/${id}`)
}
