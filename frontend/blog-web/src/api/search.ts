import request from '@/utils/request'

export interface SearchParams {
  keyword: string
  categoryId?: number | string
  tags?: string[]
  sortBy?: 'relevance' | 'time' | 'viewCount' | 'likeCount'
  sortOrder?: 'asc' | 'desc'
  current?: number
  size?: number
}

export interface SearchResult {
  id: number
  title: string
  summary: string
  contentFragment: string
  authorName: string
  categoryName: string
  tags: string[]
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  score: number
  publishTime: string
}

export interface SearchPageResult {
  records: SearchResult[]
  total: number
  current: number
  size: number
}

/** 全文搜索 */
export function searchArticles(params: SearchParams) {
  return request.post('/search/articles', params)
}

/** 搜索建议 */
export function getSearchSuggestions(keyword: string) {
  return request.get('/search/suggestions', { params: { keyword } })
}
