import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 文件信息 */
export interface FileVO {
  id: number
  fileName: string
  originalName: string
  fileUrl: string
  fileSize: number
  fileType: string
  mimeType: string
  createTime: string
}

/**
 * 上传文件
 * 路径: /system/files/upload → gateway StripPrefix → system-service /files/upload
 */
export function uploadFile(file: File, module: string = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('module', module)
  return request.post<ApiResult<FileVO>>('/system/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传文件
 */
export function uploadFiles(files: File[], module: string = 'common') {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('module', module)
  return request.post<ApiResult<FileVO[]>>('/system/files/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文件信息
 */
export function getFileById(id: number) {
  return request.get<ApiResult<FileVO>>(`/system/files/${id}`)
}

/**
 * 删除文件
 */
export function deleteFile(id: number) {
  return request.delete<ApiResult<void>>(`/system/files/${id}`)
}
