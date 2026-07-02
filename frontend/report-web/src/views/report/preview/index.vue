<template>
  <div class="report-preview">
    <n-card>
      <template #header>
        <n-space justify="space-between">
          <span>{{ report?.name || '报表预览' }}</span>
          <n-space>
            <n-button @click="handleExport('excel')">导出Excel</n-button>
            <n-button @click="handleExport('pdf')">导出PDF</n-button>
            <n-button @click="handlePrint">打印</n-button>
          </n-space>
        </n-space>
      </template>

      <!-- 参数面板 -->
      <n-card v-if="params.length > 0" title="查询条件" style="margin-bottom: 16px">
        <n-form inline :model="queryParams">
          <n-form-item v-for="param in params" :key="param.name" :label="param.label">
            <n-input v-if="param.type === 'text'" v-model:value="queryParams[param.name]" :placeholder="param.label" />
            <n-date-picker v-else-if="param.type === 'date'" v-model:value="queryParams[param.name]" type="date" />
            <n-select v-else-if="param.type === 'select'" v-model:value="queryParams[param.name]" :options="param.options" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">查询</n-button>
            <n-button @click="handleReset" style="margin-left: 8px">重置</n-button>
          </n-form-item>
        </n-form>
      </n-card>

      <!-- 报表内容 -->
      <div ref="reportContent" class="report-content" :style="canvasStyle">
        <div
          v-for="item in components"
          :key="item.id"
          class="component-wrapper"
          :style="getComponentStyle(item)"
        >
          <WidgetRenderer :config="item" :data="reportData" />
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useReportStore } from '@/stores/report'
import { useMessage } from 'naive-ui'
import { WidgetRenderer } from '@/components/widgets'

const route = useRoute()
const store = useReportStore()
const message = useMessage()

const reportContent = ref<HTMLElement | null>(null)

// 报表数据
const report = computed(() => store.current)
const reportData = computed(() => store.reportData)

// 画布样式
const canvasStyle = computed(() => ({
  width: (report.value?.config?.canvas?.width || 1200) + 'px',
  height: (report.value?.config?.canvas?.height || 800) + 'px',
  background: report.value?.config?.canvas?.background || '#ffffff',
  position: 'relative' as const
}))

// 组件列表
const components = computed(() => report.value?.config?.components || [])

// 参数列表
const params = computed(() => report.value?.config?.params || [])

// 查询参数
const queryParams = reactive<Record<string, any>>({})

// 获取组件样式
const getComponentStyle = (item: any) => ({
  position: 'absolute' as const,
  left: item.x + 'px',
  top: item.y + 'px',
  width: item.width + 'px',
  height: item.height + 'px'
})

// 搜索
const handleSearch = () => {
  const id = route.params.id
  store.fetchData(String(id), queryParams)
}

// 重置
const handleReset = () => {
  Object.keys(queryParams).forEach(key => {
    queryParams[key] = undefined
  })
  handleSearch()
}

// 导出
const handleExport = async (format: string) => {
  try {
    // TODO: 调用导出接口
    message.success(`导出${format.toUpperCase()}成功`)
  } catch {
    message.error('导出失败')
  }
}

// 打印
const handlePrint = () => {
  window.print()
}

onMounted(async () => {
  const id = route.params.id
  if (id) {
    await store.fetchById(String(id))
    handleSearch()
  }
})
</script>

<style scoped>
.report-preview {
  height: 100%;
}

.report-content {
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: auto;
}

.component-wrapper {
  border: 1px solid transparent;
  transition: border-color 0.2s;
}

.component-wrapper:hover {
  border-color: #18a058;
}

@media print {
  .report-preview :deep(.n-card) {
    box-shadow: none;
    border: none;
  }

  .report-preview :deep(.n-card-header) {
    display: none;
  }
}
</style>
