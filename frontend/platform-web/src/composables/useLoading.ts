import { ref } from 'vue'
import { useMessage } from 'naive-ui'

/**
 * 统一 Loading 和错误处理组合式函数
 */
export function useLoading(defaultValue = false) {
  const loading = ref(defaultValue)
  const message = useMessage()

  /**
   * 包装异步操作，自动处理 loading 状态和错误
   */
  async function withLoading<T>(
    fn: () => Promise<T>,
    options?: {
      /** 错误提示消息 */
      errorMsg?: string
      /** 成功提示消息 */
      successMsg?: string
      /** 是否显示错误提示，默认 true */
      showError?: boolean
      /** 错误回调 */
      onError?: (error: Error) => void
    }
  ): Promise<T | null> {
    const {
      errorMsg = '操作失败',
      successMsg,
      showError = true,
      onError
    } = options || {}

    loading.value = true
    try {
      const result = await fn()
      if (successMsg) {
        message.success(successMsg)
      }
      return result
    } catch (error: any) {
      if (showError) {
        message.error(error.message || errorMsg)
      }
      onError?.(error)
      return null
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    withLoading
  }
}

/**
 * 分页查询组合式函数
 */
export function usePagination<T>(
  fetchFn: (params: any) => Promise<any>,
  defaultPageSize = 10
) {
  const loading = ref(false)
  const data = ref<T[]>([]) as any
  const pagination = ref({
    page: 1,
    pageSize: defaultPageSize,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    prefix: ({ itemCount }: { itemCount: number }) => `共 ${itemCount} 条`
  })

  async function loadData(params: Record<string, any> = {}) {
    loading.value = true
    try {
      const { data: res } = await fetchFn({
        current: pagination.value.page,
        size: pagination.value.pageSize,
        ...params
      })
      data.value = res.data?.records || []
      pagination.value.itemCount = res.data?.total || 0
    } catch {
      data.value = []
    } finally {
      loading.value = false
    }
  }

  function handlePageChange(page: number) {
    pagination.value.page = page
    loadData()
  }

  function handlePageSizeChange(pageSize: number) {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadData()
  }

  function reset() {
    pagination.value.page = 1
    loadData()
  }

  return {
    loading,
    data,
    pagination,
    loadData,
    handlePageChange,
    handlePageSizeChange,
    reset
  }
}
