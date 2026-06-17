import request from '@/utils/request'
import type { ApiResult, PageResult, PageQuery } from '@/types/api'

/** 项目信息 */
export interface ProjectVO {
  id: number
  name: string
  description: string
  status: number
  priority: number
  ownerId: number
  startDate: string
  endDate: string
  createTime: string
  updateTime: string
}

/** 项目创建参数 */
export interface ProjectCreateParams {
  name: string
  description?: string
  priority?: number
  ownerId?: number
  startDate?: string
  endDate?: string
}

/** 项目更新参数 */
export interface ProjectUpdateParams {
  id: number
  name: string
  description?: string
  status?: number
  priority?: number
  ownerId?: number
  startDate?: string
  endDate?: string
}

/** 项目查询参数 */
export interface ProjectQueryParams extends PageQuery {
  name?: string
  status?: number
  priority?: number
  ownerId?: number
}

/** 任务信息 */
export interface TaskVO {
  id: number
  projectId: number
  parentTaskId: number
  title: string
  description: string
  status: number
  priority: number
  sourceType: string
  assigneeId: number
  reporterId: number
  blockedReason: string
  dueDate: string
  createTime: string
  updateTime: string
}

/** 任务执行记录 */
export interface TaskExecutionVO {
  id: number
  taskId: number
  executorType: string
  executorId: number
  action: string
  content: string
  prompt: string
  result: string
  status: number
  errorMsg: string
  duration: number
  createTime: string
}

/** 任务创建参数 */
export interface TaskCreateParams {
  projectId: number
  title: string
  description?: string
  priority?: number
  assigneeId?: number
  dueDate?: string
}

/** 任务更新参数 */
export interface TaskUpdateParams {
  id: number
  title: string
  description?: string
  status?: number
  priority?: number
  assigneeId?: number
  dueDate?: string
}

/** 任务查询参数 */
export interface TaskQueryParams extends PageQuery {
  projectId?: number
  title?: string
  status?: number
  priority?: number
  assigneeId?: number
}

/** Bug信息 */
export interface BugVO {
  id: number
  projectId: number
  title: string
  description: string
  severity: number
  status: number
  assigneeId: number
  reporterId: number
  dueDate: string
  createTime: string
  updateTime: string
}

/** Bug创建参数 */
export interface BugCreateParams {
  projectId: number
  title: string
  description?: string
  severity?: number
  assigneeId?: number
  dueDate?: string
}

/** Bug更新参数 */
export interface BugUpdateParams {
  id: number
  title: string
  description?: string
  severity?: number
  status?: number
  assigneeId?: number
  dueDate?: string
}

/** Bug查询参数 */
export interface BugQueryParams extends PageQuery {
  projectId?: number
  title?: string
  severity?: number
  status?: number
  assigneeId?: number
}

// ========== 项目 API ==========

/** 分页查询项目 */
export function getProjectPage(params: ProjectQueryParams) {
  return request.get<ApiResult<PageResult<ProjectVO>>>('/project/projects', { params })
}

/** 获取项目详情 */
export function getProjectById(id: number) {
  return request.get<ApiResult<ProjectVO>>(`/project/projects/${id}`)
}

/** 创建项目 */
export function createProject(data: ProjectCreateParams) {
  return request.post<ApiResult<number>>('/project/projects', data)
}

/** 更新项目 */
export function updateProject(data: ProjectUpdateParams) {
  return request.put<ApiResult<void>>('/project/projects', data)
}

/** 删除项目 */
export function deleteProject(id: number) {
  return request.delete<ApiResult<void>>(`/project/projects/${id}`)
}

/** AI拆分任务 */
export function aiDecomposeTasks(id: number, content: string) {
  return request.post<ApiResult<number[]>>(`/project/projects/${id}/ai-decompose`, { content })
}

// ========== 任务 API ==========

/** 分页查询任务 */
export function getTaskPage(params: TaskQueryParams) {
  return request.get<ApiResult<PageResult<TaskVO>>>('/project/tasks', { params })
}

/** 获取任务详情 */
export function getTaskById(id: number) {
  return request.get<ApiResult<TaskVO>>(`/project/tasks/${id}`)
}

/** 创建任务 */
export function createTask(data: TaskCreateParams) {
  return request.post<ApiResult<number>>('/project/tasks', data)
}

/** 更新任务 */
export function updateTask(data: TaskUpdateParams) {
  return request.put<ApiResult<void>>('/project/tasks', data)
}

/** 删除任务 */
export function deleteTask(id: number) {
  return request.delete<ApiResult<void>>(`/project/tasks/${id}`)
}

// ========== 任务状态流转 API ==========

/** 开始任务 (BACKLOG/READY -> DOING) */
export function startTask(id: number) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/start`)
}

/** 完成任务 (DOING/REVIEW -> DONE) */
export function completeTask(id: number) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/complete`)
}

/** 提交审核 (DOING -> REVIEW) */
export function submitReview(id: number) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/submit-review`)
}

/** 审核通过 (REVIEW -> DONE) */
export function approveTask(id: number) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/approve`)
}

/** 审核拒绝 (REVIEW -> DOING) */
export function rejectTask(id: number, reason: string) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/reject?reason=${reason}`)
}

/** 阻塞任务 (DOING -> BLOCKED) */
export function blockTask(id: number, reason: string) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/block?reason=${reason}`)
}

/** 解除阻塞 (BLOCKED -> DOING) */
export function unblockTask(id: number) {
  return request.post<ApiResult<void>>(`/project/tasks/${id}/unblock`)
}

/** 获取任务执行记录 */
export function getTaskExecutions(id: number) {
  return request.get<ApiResult<TaskExecutionVO[]>>(`/project/tasks/${id}/executions`)
}

// ========== Agent 执行 API ==========

/** 触发 Agent 执行任务 */
export function executeAgentTask(taskId: number) {
  return request.post<ApiResult<void>>(`/agent/tasks/${taskId}/execute`)
}

// ========== Bug API ==========

/** 分页查询Bug */
export function getBugPage(params: BugQueryParams) {
  return request.get<ApiResult<PageResult<BugVO>>>('/project/bugs', { params })
}

/** 获取Bug详情 */
export function getBugById(id: number) {
  return request.get<ApiResult<BugVO>>(`/project/bugs/${id}`)
}

/** 创建Bug */
export function createBug(data: BugCreateParams) {
  return request.post<ApiResult<number>>('/project/bugs', data)
}

/** 更新Bug */
export function updateBug(data: BugUpdateParams) {
  return request.put<ApiResult<void>>('/project/bugs', data)
}

/** 删除Bug */
export function deleteBug(id: number) {
  return request.delete<ApiResult<void>>(`/project/bugs/${id}`)
}
