import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 注册请求参数 */
export interface RegisterParams {
  username: string
  password: string
  nickname?: string
  email?: string
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  roles: string[]
  permissions: string[]
}

/** 登录响应 */
export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

/**
 * 用户登录
 */
export function login(data: LoginParams) {
  return request.post<ApiResult<LoginResult>>('/auth/login', data)
}

/**
 * 用户注册
 */
export function register(data: RegisterParams) {
  return request.post<ApiResult<void>>('/auth/register', data)
}

/**
 * 退出登录
 */
export function logout() {
  return request.post<ApiResult<void>>('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request.get<ApiResult<UserInfo>>('/auth/user-info')
}
