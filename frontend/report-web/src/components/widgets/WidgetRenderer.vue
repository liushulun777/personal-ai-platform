<template>
  <div class="widget-renderer" :style="containerStyle">
    <component :is="widgetComponent" :config="config" :data="widgetData" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import TextWidget from './TextWidget.vue'
import TableWidget from './TableWidget.vue'
import ChartWidget from './ChartWidget.vue'
import ImageWidget from './ImageWidget.vue'

interface Props {
  config: any
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  data: undefined
})

const widgets: Record<string, any> = {
  text: TextWidget,
  table: TableWidget,
  chart: ChartWidget,
  image: ImageWidget,
  bar: ChartWidget,
  line: ChartWidget,
  pie: ChartWidget,
  gauge: ChartWidget
}

const widgetComponent = computed(() => {
  const type = props.config?.type || 'text'
  return widgets[type] || TextWidget
})

const widgetData = computed(() => {
  // 从全局数据中获取当前组件的数据
  if (props.data && props.config?.datasetId) {
    return props.data[props.config.datasetId] || []
  }
  return props.data
})

const containerStyle = computed(() => ({
  width: '100%',
  height: '100%',
  overflow: 'hidden'
}))
</script>

<style scoped>
.widget-renderer {
  position: relative;
}
</style>
