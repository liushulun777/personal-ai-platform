import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** 角色信息 */
export interface RoleVO {
  id: number
  roleName: string
  roleKey: string
  dataScope?: number
}

/** 角色详情 */
export interface RoleDetailVO extends RoleVO {
  description: string
  sort: number
  status: number
  menuIds: number[]
}

/** 角色查询参数 */
export interface RoleQueryParams extends PageQuery {
  roleName?: string
  roleKey?: string
  status?: number
}

/** 创建角色参数 */
export interface RoleCreateParams {
  roleName: string
  roleKey: string
  description?: string
  sort?: number
  status?: number
  dataScope?: number
  menuIds?: number[]
}

/** 更新角色参数 */
export interface RoleUpdateParams {
  roleName: string
  roleKey: string
  description?: string
  sort?: number
  status?: number
  dataScope?: number
  menuIds?: number[]
}

/**
 * 分页查询角色
 */
export function getRolePage(params: RoleQueryParams) {
  return request.get<ApiResult<PageResult<RoleVO>>>('/system/roles', { params })
}

/**
 * 获取所有角色列表
 */
export function getAllRoles() {
  return request.get<ApiResult<RoleVO[]>>('/system/roles/all')
}

/**
 * 获取角色详情
 */
export function getRoleById(id: number) {
  return request.get<ApiResult<RoleDetailVO>>(`/system/roles/${id}`)
}

/**
 * 创建角色
 */
export function createRole(data: RoleCreateParams) {
  return request.post<ApiResult<number>>('/system/roles', data)
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: RoleUpdateParams) {
  return request.put<ApiResult<void>>(`/system/roles/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number) {
  return request.delete<ApiResult<void>>(`/system/roles/${id}`)
}

/**
 * 分配菜单
 */
export function assignMenus(id: number, menuIds: number[]) {
  return request.put<ApiResult<void>>(`/system/roles/${id}/menus`, menuIds)
}
