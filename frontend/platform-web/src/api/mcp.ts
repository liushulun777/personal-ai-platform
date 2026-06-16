import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** MCP 服务信息 */
export interface McpServerVO {
  id: number
  name: string
  description: string
  transportType: string
  endpoint: string
  command: string
  status: number
  authorId: number
  toolCount: number
  createTime: string
  updateTime: string
}

/** MCP 服务详情 */
export interface McpServerDetailVO extends McpServerVO {
  args: string
  envVars: string
  tools: McpToolVO[]
  resources: McpResourceVO[]
}

/** MCP 服务创建参数 */
export interface McpServerCreateParams {
  name: string
  description?: string
  transportType: string
  endpoint?: string
  command?: string
  args?: string
  envVars?: string
}

/** MCP 服务更新参数 */
export interface McpServerUpdateParams {
  name?: string
  description?: string
  transportType?: string
  endpoint?: string
  command?: string
  args?: string
  envVars?: string
}

/** MCP 服务查询参数 */
export interface McpServerQueryParams extends PageQuery {
  name?: string
  status?: number
}

/** MCP 工具信息 */
export interface McpToolVO {
  id: number
  serverId: number
  serverName: string
  name: string
  description: string
  inputSchema: string
  outputSchema: string
  status: number
  createTime: string
}

/** MCP 工具创建参数 */
export interface McpToolCreateParams {
  serverId: number
  name: string
  description?: string
  inputSchema?: string
  outputSchema?: string
}

/** MCP 工具查询参数 */
export interface McpToolQueryParams extends PageQuery {
  serverId?: number
  name?: string
  status?: number
}

/** MCP 工具调用参数 */
export interface McpToolInvokeParams {
  toolId: number
  arguments?: Record<string, unknown>
}

/** MCP 资源信息 */
export interface McpResourceVO {
  id: number
  serverId: number
  uri: string
  name: string
  description: string
  mimeType: string
  status: number
  createTime: string
}

// ========== MCP 服务 API ==========

/** 创建 MCP 服务 */
export function createMcpServer(data: McpServerCreateParams) {
  return request.post<ApiResult<number>>('/mcp/servers', data)
}

/** 更新 MCP 服务 */
export function updateMcpServer(id: number, data: McpServerUpdateParams) {
  return request.put<ApiResult<void>>(`/mcp/servers/${id}`, data)
}

/** 删除 MCP 服务 */
export function deleteMcpServer(id: number) {
  return request.delete<ApiResult<void>>(`/mcp/servers/${id}`)
}

/** 获取 MCP 服务详情 */
export function getMcpServerById(id: number) {
  return request.get<ApiResult<McpServerDetailVO>>(`/mcp/servers/${id}`)
}

/** 分页查询 MCP 服务 */
export function getMcpServerPage(params: McpServerQueryParams) {
  return request.get<ApiResult<PageResult<McpServerVO>>>('/mcp/servers', { params })
}

/** 启用 MCP 服务 */
export function enableMcpServer(id: number) {
  return request.put<ApiResult<void>>(`/mcp/servers/${id}/enable`)
}

/** 禁用 MCP 服务 */
export function disableMcpServer(id: number) {
  return request.put<ApiResult<void>>(`/mcp/servers/${id}/disable`)
}

// ========== MCP 工具 API ==========

/** 创建 MCP 工具 */
export function createMcpTool(data: McpToolCreateParams) {
  return request.post<ApiResult<number>>('/mcp/tools', data)
}

/** 删除 MCP 工具 */
export function deleteMcpTool(id: number) {
  return request.delete<ApiResult<void>>(`/mcp/tools/${id}`)
}

/** 获取 MCP 工具详情 */
export function getMcpToolById(id: number) {
  return request.get<ApiResult<McpToolVO>>(`/mcp/tools/${id}`)
}

/** 分页查询 MCP 工具 */
export function getMcpToolPage(params: McpToolQueryParams) {
  return request.get<ApiResult<PageResult<McpToolVO>>>('/mcp/tools', { params })
}

/** 调用 MCP 工具 */
export function invokeMcpTool(data: McpToolInvokeParams) {
  return request.post<ApiResult<Record<string, unknown>>>('/mcp/tools/invoke', data)
}

/** 从 MCP Server 同步工具列表 */
export function syncMcpTools(serverId: number) {
  return request.post<ApiResult<void>>(`/mcp/tools/sync/${serverId}`)
}
