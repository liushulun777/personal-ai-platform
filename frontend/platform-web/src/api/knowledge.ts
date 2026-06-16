import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** 文档信息 */
export interface DocumentVO {
  id: number
  title: string
  fileType: string
  fileSize: number
  chunkCount: number
  status: number
  categoryId: number
  authorId: number
  createTime: string
}

/** 文档详情 */
export interface DocumentDetailVO extends DocumentVO {
  fileUrl: string
  content: string
}

/** 文档上传参数 */
export interface DocumentUploadParams {
  title: string
  categoryId?: number
  chunkSize?: number
  chunkOverlap?: number
}

/** 文档查询参数 */
export interface DocumentQueryParams extends PageQuery {
  title?: string
  fileType?: string
  status?: number
  categoryId?: number
}

/** 知识库问答查询参数 */
export interface KnowledgeQueryParams {
  question: string
  topK?: number
  categoryId?: number
}

/** 来源信息 */
export interface SourceInfo {
  documentId: number
  documentTitle: string
  chunkContent: string
  similarity: number
}

/** 知识库问答响应 */
export interface KnowledgeAnswerVO {
  answer: string
  sources: SourceInfo[]
}

/** 分页查询文档 */
export function getDocumentPage(params: DocumentQueryParams) {
  return request.get<ApiResult<PageResult<DocumentVO>>>('/knowledge/documents', { params })
}

/** 获取文档详情 */
export function getDocumentById(id: number) {
  return request.get<ApiResult<DocumentDetailVO>>(`/knowledge/documents/${id}`)
}

/** 上传文档 */
export function uploadDocument(file: File, dto: DocumentUploadParams) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dto', new Blob([JSON.stringify(dto)], { type: 'application/json' }))
  return request.post<ApiResult<number>>('/knowledge/documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 删除文档 */
export function deleteDocument(id: number) {
  return request.delete<ApiResult<void>>(`/knowledge/documents/${id}`)
}

/** 重新处理文档 */
export function reprocessDocument(id: number) {
  return request.post<ApiResult<void>>(`/knowledge/documents/${id}/reprocess`)
}

/** 知识库问答 */
export function queryKnowledge(data: KnowledgeQueryParams) {
  return request.post<ApiResult<KnowledgeAnswerVO>>('/knowledge/knowledge/query', data)
}
