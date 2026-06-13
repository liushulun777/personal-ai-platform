import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** 用户信息 */
export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createTime: string
}

/** 用户详情 */
export interface UserDetailVO extends UserVO {
  roles: RoleVO[]
  roleIds: number[]
}

/** 角色信息 */
export interface RoleVO {
  id: number
  roleName: string
  roleKey: string
}

/** 用户查询参数 */
export interface UserQueryParams extends PageQuery {
  username?: string
  nickname?: string
  phone?: string
  status?: number
}

/** 创建用户参数 */
export interface UserCreateParams {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  status?: number
  roleIds?: number[]
}

/** 更新用户参数 */
export interface UserUpdateParams {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  roleIds?: number[]
}

/** 用户状态参数 */
export interface UserStatusParams {
  status: number
}

/**
 * 分页查询用户
 */
export function getUserPage(params: UserQueryParams) {
  return request.get<ApiResult<PageResult<UserVO>>>('/system/users', { params })
}

/**
 * 获取用户详情
 */
export function getUserById(id: number) {
  return request.get<ApiResult<UserDetailVO>>(`/system/users/${id}`)
}

/**
 * 创建用户
 */
export function createUser(data: UserCreateParams) {
  return request.post<ApiResult<number>>('/system/users', data)
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: UserUpdateParams) {
  return request.put<ApiResult<void>>(`/system/users/${id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return request.delete<ApiResult<void>>(`/system/users/${id}`)
}

/**
 * 修改用户状态
 */
export function updateUserStatus(id: number, data: UserStatusParams) {
  return request.put<ApiResult<void>>(`/system/users/${id}/status`, data)
}

/**
 * 分配角色
 */
export function assignRoles(id: number, roleIds: number[]) {
  return request.put<ApiResult<void>>(`/system/users/${id}/roles`, roleIds)
}
