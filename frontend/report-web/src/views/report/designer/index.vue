<template>
  <div class="report-designer">
    <n-layout has-sider style="height: calc(100vh - 112px)">
      <!-- 左侧组件面板 -->
      <n-layout-sider bordered :width="280" content-style="padding: 16px;">
        <!-- 数据集选择 -->
        <n-card title="数据集" size="small" style="margin-bottom: 16px;">
          <n-select
            v-model:value="selectedDatasetId"
            :options="datasetOptions"
            placeholder="选择数据集"
            clearable
            @update:value="handleDatasetChange"
          />
          <div v-if="currentDatasetFields.length > 0" class="field-list">
            <div class="field-title">可用字段：</div>
            <n-space>
              <n-tag
                v-for="field in currentDatasetFields"
                :key="field.name"
                size="small"
                :type="getFieldTagType(field.type) as any"
                draggable="true"
                @dragstart="handleFieldDragStart($event, field)"
              >
                {{ field.label }}
              </n-tag>
            </n-space>
          </div>
        </n-card>

        <!-- 组件库 -->
        <n-collapse default-expanded-names="basic">
          <n-collapse-item title="基础组件" name="basic">
            <div class="component-list">
              <div v-for="item in basicComponents" :key="item.type" class="component-item" draggable="true" @dragstart="handleDragStart($event, item)">
                <n-icon size="20"><svg viewBox="0 0 24 24"><path fill="currentColor" :d="item.icon"/></svg></n-icon>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </n-collapse-item>
          <n-collapse-item title="图表组件" name="chart">
            <div class="component-list">
              <div v-for="item in chartComponents" :key="item.type" class="component-item" draggable="true" @dragstart="handleDragStart($event, item)">
                <n-icon size="20"><svg viewBox="0 0 24 24"><path fill="currentColor" :d="item.icon"/></svg></n-icon>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </n-collapse-item>
          <n-collapse-item title="模板" name="template">
            <div class="component-list">
              <div v-for="item in templates" :key="item.name" class="component-item template-item" @click="handleApplyTemplate(item)">
                <n-icon size="20" color="#18a058"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/></svg></n-icon>
                <span>{{ item.name }}</span>
              </div>
            </div>
          </n-collapse-item>
        </n-collapse>
      </n-layout-sider>

      <!-- 中间画布 -->
      <n-layout-content content-style="padding: 16px; background: #f0f2f5;">
        <div class="canvas-toolbar">
          <n-space>
            <n-button size="small" @click="handleZoomOut">缩小</n-button>
            <span>{{ Math.round(scale * 100) }}%</span>
            <n-button size="small" @click="handleZoomIn">放大</n-button>
            <n-button size="small" @click="handleResetZoom">重置</n-button>
          </n-space>
          <n-space>
            <n-button size="small" @click="handlePreview">预览</n-button>
            <n-button size="small" type="primary" @click="handleSave">保存</n-button>
          </n-space>
        </div>
        <div class="canvas-container">
          <div
            class="canvas"
            :style="{ width: canvasConfig.width + 'px', height: canvasConfig.height + 'px', background: canvasConfig.background, transform: `scale(${scale})` }"
            @drop="handleDrop"
            @dragover.prevent
          >
            <div
              v-for="item in components"
              :key="item.id"
              class="component"
              :class="{ active: activeComponent?.id === item.id }"
              :style="getComponentStyle(item)"
              @click="handleSelect(item)"
            >
              <WidgetRenderer :config="item" :data="reportData" />
              <div class="component-label">{{ item.config?.title || item.type }}</div>
              <div v-if="activeComponent?.id === item.id" class="resize-handle" @mousedown.stop="handleResizeStart($event, item)" />
            </div>
          </div>
        </div>
      </n-layout-content>

      <!-- 右侧属性面板 -->
      <n-layout-sider bordered :width="320" content-style="padding: 16px; overflow-y: auto;">
        <n-tabs>
          <n-tab-pane name="component" tab="组件属性">
            <template v-if="activeComponent">
              <n-form label-placement="left" label-width="80" size="small">
                <n-form-item label="组件类型">
                  <n-input :value="getComponentLabel(activeComponent.type)" disabled />
                </n-form-item>
                <n-divider>位置和大小</n-divider>
                <n-grid :cols="2">
                  <n-gi>
                    <n-form-item label="X">
                      <n-input-number v-model:value="activeComponent.x" :min="0" size="small" />
                    </n-form-item>
                  </n-gi>
                  <n-gi>
                    <n-form-item label="Y">
                      <n-input-number v-model:value="activeComponent.y" :min="0" size="small" />
                    </n-form-item>
                  </n-gi>
                  <n-gi>
                    <n-form-item label="宽度">
                      <n-input-number v-model:value="activeComponent.width" :min="50" size="small" />
                    </n-form-item>
                  </n-gi>
                  <n-gi>
                    <n-form-item label="高度">
                      <n-input-number v-model:value="activeComponent.height" :min="30" size="small" />
                    </n-form-item>
                  </n-gi>
                </n-grid>

                <n-divider>数据配置</n-divider>
                <n-form-item label="数据集">
                  <n-select
                    v-model:value="activeComponent.config.datasetId"
                    :options="datasetOptions"
                    placeholder="选择数据集"
                    clearable
                    size="small"
                  />
                </n-form-item>

                <!-- 文本组件配置 -->
                <template v-if="activeComponent.type === 'text'">
                  <n-form-item label="文本内容">
                    <n-input v-model:value="activeComponent.config.content" type="textarea" :rows="3" />
                  </n-form-item>
                  <n-form-item label="字体大小">
                    <n-input-number v-model:value="activeComponent.config.fontSize" :min="12" :max="72" size="small" />
                  </n-form-item>
                  <n-form-item label="字体颜色">
                    <n-color-picker v-model:value="activeComponent.config.color" size="small" />
                  </n-form-item>
                  <n-form-item label="加粗">
                    <n-switch v-model:value="activeComponent.config.fontBold" />
                  </n-form-item>
                </template>

                <!-- 图表组件配置 -->
                <template v-if="['bar', 'line', 'pie', 'gauge'].includes(activeComponent.type)">
                  <n-form-item label="图表标题">
                    <n-input v-model:value="activeComponent.config.title" size="small" />
                  </n-form-item>
                  <n-form-item label="X轴字段">
                    <n-select
                      v-model:value="activeComponent.config.xField"
                      :options="getFieldOptions(activeComponent.config.datasetId)"
                      placeholder="选择字段"
                      size="small"
                    />
                  </n-form-item>
                  <n-form-item label="Y轴字段">
                    <n-select
                      v-model:value="activeComponent.config.yField"
                      :options="getFieldOptions(activeComponent.config.datasetId)"
                      placeholder="选择字段"
                      size="small"
                    />
                  </n-form-item>
                </template>

                <!-- 表格组件配置 -->
                <template v-if="activeComponent.type === 'table'">
                  <n-form-item label="显示列数">
                    <n-input-number v-model:value="activeComponent.config.pageSize" :min="5" :max="50" size="small" />
                  </n-form-item>
                </template>
              </n-form>
            </template>
            <n-empty v-else description="请选择组件" />
          </n-tab-pane>
          <n-tab-pane name="canvas" tab="画布属性">
            <n-form label-placement="left" label-width="80" size="small">
              <n-form-item label="宽度">
                <n-input-number v-model:value="canvasConfig.width" :min="800" :max="3840" size="small" />
              </n-form-item>
              <n-form-item label="高度">
                <n-input-number v-model:value="canvasConfig.height" :min="600" :max="2160" size="small" />
              </n-form-item>
              <n-form-item label="背景色">
                <n-color-picker v-model:value="canvasConfig.background" size="small" />
              </n-form-item>
            </n-form>
          </n-tab-pane>
        </n-tabs>

        <!-- 操作按钮 -->
        <div style="margin-top: 16px;">
          <n-space vertical>
            <n-button v-if="activeComponent" type="error" block size="small" @click="handleDelete">删除组件</n-button>
            <n-button v-if="activeComponent" block size="small" @click="handleDuplicate">复制组件</n-button>
          </n-space>
        </div>
      </n-layout-sider>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReportStore } from '@/stores/report'
import { useDataSetStore } from '@/stores/dataset'
import { useMessage } from 'naive-ui'
import { WidgetRenderer } from '@/components/widgets'

const route = useRoute()
const router = useRouter()
const reportStore = useReportStore()
const datasetStore = useDataSetStore()
const message = useMessage()

// 画布配置
const canvasConfig = reactive({
  width: 1200,
  height: 800,
  background: '#ffffff'
})

// 缩放比例
const scale = ref(0.8)

// 组件列表
const components = ref<any[]>([])

// 当前选中的组件
const activeComponent = ref<any>(null)

// 选中的数据集
const selectedDatasetId = ref<string | null>(null)

// 数据集选项
const datasetOptions = computed(() => {
  return datasetStore.list.map(ds => ({
    label: ds.name,
    value: ds.id
  }))
})

// 当前数据集字段
const currentDatasetFields = computed(() => {
  if (!selectedDatasetId.value) return []
  const dataset = datasetStore.list.find(ds => ds.id === selectedDatasetId.value)
  return dataset?.fields || []
})

// 报表数据
const reportData = computed(() => reportStore.reportData)

// SVG路径常量
const SVG_PATHS = {
  text: 'M3 3h18v18H3V3zm2 2v14h14V5H5zm2 2h10v2H7V7zm0 4h10v2H7v-2zm0 4h7v2H7v-2z',
  table: 'M3 3h18v18H3V3zm2 2v14h14V5H5zm2 2h4v4H7V7zm6 0h4v4h-4V7zm-6 6h4v4H7v-4zm6 0h4v4h-4v-4z',
  bar: 'M5 9.2h3V19H5V9.2zM10.6 5h2.8v14h-2.8V5zm5.6 8H19v6h-2.8v-6z',
  line: 'M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z',
  pie: 'M11 2v20c-5.07-.5-9-4.79-9-10s3.93-9.5 9-10zm2 0v9h9c-.47-4.69-4.3-8.53-9-9zm0 11v9c4.69-.47 8.53-4.3 9-9h-9z',
  gauge: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z'
}

// 基础组件
const basicComponents = [
  { type: 'text', label: '文本', icon: SVG_PATHS.text },
  { type: 'table', label: '表格', icon: SVG_PATHS.table }
]

// 图表组件
const chartComponents = [
  { type: 'bar', label: '柱状图', icon: SVG_PATHS.bar },
  { type: 'line', label: '折线图', icon: SVG_PATHS.line },
  { type: 'pie', label: '饼图', icon: SVG_PATHS.pie },
  { type: 'gauge', label: '仪表盘', icon: SVG_PATHS.gauge }
]

// 报表模板
const templates = [
  {
    name: '数据概览',
    components: [
      { type: 'text', x: 20, y: 20, width: 400, height: 40, config: { content: '数据概览', fontSize: 24, fontBold: true, color: '#333' } },
      { type: 'bar', x: 20, y: 80, width: 560, height: 350, config: { title: '数据趋势', chartType: 'bar' } },
      { type: 'pie', x: 600, y: 80, width: 560, height: 350, config: { title: '数据分布', chartType: 'pie' } },
      { type: 'table', x: 20, y: 450, width: 1160, height: 300, config: { title: '数据明细' } }
    ]
  },
  {
    name: '趋势分析',
    components: [
      { type: 'text', x: 20, y: 20, width: 400, height: 40, config: { content: '趋势分析', fontSize: 24, fontBold: true, color: '#333' } },
      { type: 'line', x: 20, y: 80, width: 1160, height: 400, config: { title: '趋势图', chartType: 'line' } },
      { type: 'table', x: 20, y: 500, width: 1160, height: 250, config: { title: '数据明细' } }
    ]
  }
]

// 获取字段标签类型
const getFieldTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'NUMBER': 'success',
    'DATE': 'warning',
    'STRING': 'info',
    'BOOLEAN': 'error'
  }
  return typeMap[type] || 'default'
}

// 获取字段选项
const getFieldOptions = (datasetId: string) => {
  if (!datasetId) return []
  const dataset = datasetStore.list.find(ds => ds.id === datasetId)
  if (!dataset?.fields) return []
  return dataset.fields.map((f: any) => ({
    label: f.label || f.name,
    value: f.name
  }))
}

// 获取组件标签
const getComponentLabel = (type: string) => {
  const labels: Record<string, string> = {
    text: '文本',
    table: '表格',
    bar: '柱状图',
    line: '折线图',
    pie: '饼图',
    gauge: '仪表盘'
  }
  return labels[type] || type
}

// 数据集变更
const handleDatasetChange = (value: string | null) => {
  selectedDatasetId.value = value
}

// 字段拖拽开始
const handleFieldDragStart = (e: DragEvent, field: any) => {
  e.dataTransfer?.setData('field', JSON.stringify(field))
}

// 组件拖拽开始
const handleDragStart = (e: DragEvent, item: any) => {
  e.dataTransfer?.setData('component', JSON.stringify(item))
}

// 放置
const handleDrop = (e: DragEvent) => {
  const fieldData = e.dataTransfer?.getData('field')
  const componentData = e.dataTransfer?.getData('component')

  if (fieldData) {
    // 字段拖拽 - 创建表格组件
    const field = JSON.parse(fieldData)
    const rect = (e.target as HTMLElement).getBoundingClientRect()
    components.value.push({
      id: Date.now(),
      type: 'table',
      x: (e.clientX - rect.left) / scale.value,
      y: (e.clientY - rect.top) / scale.value,
      width: 400,
      height: 200,
      config: {
        title: field.label,
        datasetId: selectedDatasetId.value,
        fields: [field.name]
      }
    })
  } else if (componentData) {
    // 组件拖拽
    const item = JSON.parse(componentData)
    const rect = (e.target as HTMLElement).getBoundingClientRect()
    const newComponent: any = {
      id: Date.now(),
      type: item.type,
      x: (e.clientX - rect.left) / scale.value,
      y: (e.clientY - rect.top) / scale.value,
      width: item.type === 'text' ? 300 : 400,
      height: item.type === 'text' ? 40 : 300,
      config: {
        datasetId: selectedDatasetId.value,
        chartType: item.type
      }
    }

    // 根据组件类型设置默认配置
    if (item.type === 'text') {
      newComponent.config.content = '文本内容'
      newComponent.config.fontSize = 14
      newComponent.config.color = '#333'
    } else if (item.type === 'table') {
      newComponent.config.title = '数据表格'
    } else {
      newComponent.config.title = getComponentLabel(item.type)
      // 自动设置字段
      if (currentDatasetFields.value.length > 0) {
        const stringField = currentDatasetFields.value.find((f: any) => f.type === 'STRING')
        const numberField = currentDatasetFields.value.find((f: any) => f.type === 'NUMBER')
        if (stringField) newComponent.config.xField = stringField.name
        if (numberField) newComponent.config.yField = numberField.name
      }
    }

    components.value.push(newComponent)
  }
}

// 选择组件
const handleSelect = (item: any) => {
  activeComponent.value = item
}

// 获取组件样式
const getComponentStyle = (item: any) => ({
  left: item.x + 'px',
  top: item.y + 'px',
  width: item.width + 'px',
  height: item.height + 'px'
})

// 调整大小开始
const handleResizeStart = (e: MouseEvent, item: any) => {
  const startX = e.clientX
  const startY = e.clientY
  const startWidth = item.width
  const startHeight = item.height

  const handleMouseMove = (e: MouseEvent) => {
    item.width = Math.max(50, startWidth + (e.clientX - startX) / scale.value)
    item.height = Math.max(30, startHeight + (e.clientY - startY) / scale.value)
  }

  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 删除组件
const handleDelete = () => {
  if (!activeComponent.value) return
  const index = components.value.findIndex(c => c.id === activeComponent.value.id)
  if (index > -1) {
    components.value.splice(index, 1)
    activeComponent.value = null
  }
}

// 复制组件
const handleDuplicate = () => {
  if (!activeComponent.value) return
  const newComponent = {
    ...JSON.parse(JSON.stringify(activeComponent.value)),
    id: Date.now(),
    x: activeComponent.value.x + 20,
    y: activeComponent.value.y + 20
  }
  components.value.push(newComponent)
  activeComponent.value = newComponent
}

// 应用模板
const handleApplyTemplate = (template: any) => {
  components.value = template.components.map((c: any, index: number) => ({
    ...c,
    id: Date.now() + index,
    config: {
      ...c.config,
      datasetId: selectedDatasetId.value
    }
  }))
  message.success('模板已应用')
}

// 缩放控制
const handleZoomIn = () => {
  scale.value = Math.min(2, scale.value + 0.1)
}

const handleZoomOut = () => {
  scale.value = Math.max(0.3, scale.value - 0.1)
}

const handleResetZoom = () => {
  scale.value = 0.8
}

// 保存
const handleSave = async () => {
  const id = route.params.id
  if (!id) return

  try {
    await reportStore.update(String(id), {
      config: {
        canvas: { ...canvasConfig },
        components: components.value
      }
    })
    message.success('保存成功')
  } catch {
    message.error('保存失败')
  }
}

// 预览
const handlePreview = () => {
  const id = route.params.id
  if (id) {
    router.push(`/report/preview/${id}`)
  }
}

onMounted(async () => {
  // 加载数据集列表
  await datasetStore.fetchList({ pageNum: 1, pageSize: 100 })

  // 加载报表配置
  const id = route.params.id
  if (id) {
    await reportStore.fetchById(String(id))
    if (reportStore.current) {
      canvasConfig.width = reportStore.current.config?.canvas?.width || 1200
      canvasConfig.height = reportStore.current.config?.canvas?.height || 800
      canvasConfig.background = reportStore.current.config?.canvas?.background || '#ffffff'
      components.value = reportStore.current.config?.components || []
    }
  }
})
</script>

<style scoped>
.report-designer {
  height: 100%;
}

.field-list {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.field-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.component-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.component-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: grab;
  transition: all 0.2s;
  font-size: 12px;
}

.component-item:hover {
  border-color: #18a058;
  background: #f0fff4;
}

.template-item {
  cursor: pointer;
  background: #f5f7fa;
}

.template-item:hover {
  background: #e8f5e9;
}

.canvas-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px;
  background: #fff;
  border-radius: 4px;
}

.canvas-container {
  display: flex;
  justify-content: center;
  overflow: auto;
  height: calc(100% - 50px);
}

.canvas {
  position: relative;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform-origin: top center;
}

.component {
  position: absolute;
  border: 1px solid transparent;
  cursor: move;
  transition: border-color 0.2s;
}

.component:hover {
  border-color: #18a058;
}

.component.active {
  border-color: #18a058;
  border-width: 2px;
}

.component-label {
  position: absolute;
  top: -20px;
  left: 0;
  font-size: 12px;
  color: #18a058;
  background: #fff;
  padding: 0 4px;
  display: none;
}

.component:hover .component-label,
.component.active .component-label {
  display: block;
}

.resize-handle {
  position: absolute;
  right: -4px;
  bottom: -4px;
  width: 8px;
  height: 8px;
  background: #18a058;
  cursor: se-resize;
}
</style>
