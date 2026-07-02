import { get, post } from '@/utils/request'

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
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
export const login = (data: LoginParams) => {
  return post<LoginResult>('/auth/login', data)
}

/**
 * 退出登录
 */
export const logout = () => {
  return post<void>('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export const getUserInfo = () => {
  return get<UserInfo>('/auth/user-info')
}
