import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** 字典类型信息 */
export interface DictTypeVO {
  id: number
  dictName: string
  dictType: string
  description: string
  status: number
  createTime: string
}

/** 字典类型查询参数 */
export interface DictTypeQueryParams extends PageQuery {
  dictName?: string
  dictType?: string
  status?: number
}

/** 创建字典类型参数 */
export interface DictTypeCreateParams {
  dictName: string
  dictType: string
  description?: string
  status?: number
}

/** 更新字典类型参数 */
export interface DictTypeUpdateParams {
  dictName: string
  dictType: string
  description?: string
  status?: number
}

/** 字典数据信息 */
export interface DictDataVO {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  sort: number
  status: number
  remark: string
  createTime: string
}

/** 字典数据查询参数 */
export interface DictDataQueryParams extends PageQuery {
  dictType?: string
  dictLabel?: string
  status?: number
}

/** 创建字典数据参数 */
export interface DictDataCreateParams {
  dictType: string
  dictLabel: string
  dictValue: string
  sort?: number
  status?: number
  remark?: string
}

/** 更新字典数据参数 */
export interface DictDataUpdateParams {
  dictType: string
  dictLabel: string
  dictValue: string
  sort?: number
  status?: number
  remark?: string
}

// ========== 字典类型 ==========

/** 分页查询字典类型 */
export function getDictTypePage(params: DictTypeQueryParams) {
  return request.get<ApiResult<PageResult<DictTypeVO>>>('/system/dict/types', { params })
}

/** 获取所有字典类型列表 */
export function getAllDictTypes() {
  return request.get<ApiResult<DictTypeVO[]>>('/system/dict/types/all')
}

/** 获取字典类型详情 */
export function getDictTypeById(id: number) {
  return request.get<ApiResult<DictTypeVO>>(`/system/dict/types/${id}`)
}

/** 创建字典类型 */
export function createDictType(data: DictTypeCreateParams) {
  return request.post<ApiResult<number>>('/system/dict/types', data)
}

/** 更新字典类型 */
export function updateDictType(id: number, data: DictTypeUpdateParams) {
  return request.put<ApiResult<void>>(`/system/dict/types/${id}`, data)
}

/** 删除字典类型 */
export function deleteDictType(id: number) {
  return request.delete<ApiResult<void>>(`/system/dict/types/${id}`)
}

// ========== 字典数据 ==========

/** 分页查询字典数据 */
export function getDictDataPage(params: DictDataQueryParams) {
  return request.get<ApiResult<PageResult<DictDataVO>>>('/system/dict/data', { params })
}

/** 根据字典类型获取字典数据列表 */
export function getDictDataByType(dictType: string) {
  return request.get<ApiResult<DictDataVO[]>>(`/system/dict/data/type/${dictType}`)
}

/** 获取字典数据详情 */
export function getDictDataById(id: number) {
  return request.get<ApiResult<DictDataVO>>(`/system/dict/data/${id}`)
}

/** 创建字典数据 */
export function createDictData(data: DictDataCreateParams) {
  return request.post<ApiResult<number>>('/system/dict/data', data)
}

/** 更新字典数据 */
export function updateDictData(id: number, data: DictDataUpdateParams) {
  return request.put<ApiResult<void>>(`/system/dict/data/${id}`, data)
}

/** 删除字典数据 */
export function deleteDictData(id: number) {
  return request.delete<ApiResult<void>>(`/system/dict/data/${id}`)
}
