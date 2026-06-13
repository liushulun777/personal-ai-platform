import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 菜单信息 */
export interface MenuVO {
  id: number
  parentId: number
  menuName: string
  path: string
  component: string
  icon: string
  menuType: number
  permission: string
  sort: number
  visible: number
  status: number
  createTime: string
  children?: MenuVO[]
}

/** 创建菜单参数 */
export interface MenuCreateParams {
  parentId: number
  menuName: string
  path?: string
  component?: string
  icon?: string
  menuType: number
  permission?: string
  sort?: number
  visible?: number
  status?: number
}

/** 更新菜单参数 */
export interface MenuUpdateParams {
  parentId: number
  menuName: string
  path?: string
  component?: string
  icon?: string
  menuType: number
  permission?: string
  sort?: number
  visible?: number
  status?: number
}

/**
 * 获取菜单树形列表
 */
export function getMenuTree() {
  return request.get<ApiResult<MenuVO[]>>('/system/menus/tree')
}

/**
 * 获取菜单详情
 */
export function getMenuById(id: number) {
  return request.get<ApiResult<MenuVO>>(`/system/menus/${id}`)
}

/**
 * 创建菜单
 */
export function createMenu(data: MenuCreateParams) {
  return request.post<ApiResult<number>>('/system/menus', data)
}

/**
 * 更新菜单
 */
export function updateMenu(id: number, data: MenuUpdateParams) {
  return request.put<ApiResult<void>>(`/system/menus/${id}`, data)
}

/**
 * 删除菜单
 */
export function deleteMenu(id: number) {
  return request.delete<ApiResult<void>>(`/system/menus/${id}`)
}
