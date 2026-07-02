<template>
  <div class="chart-widget">
    <component :is="chartComponent" :data="chartData" :option="chartOption" :xField="xField" :yField="yField" :title="title" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { BarChart, LineChart, PieChart, GaugeChart } from '@/components/charts'

interface Props {
  config: any
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  data: undefined
})

const chartComponents: Record<string, any> = {
  bar: BarChart,
  line: LineChart,
  pie: PieChart,
  gauge: GaugeChart
}

const chartComponent = computed(() => {
  const type = props.config?.chartType || 'bar'
  return chartComponents[type] || BarChart
})

const chartData = computed(() => {
  if (Array.isArray(props.data)) {
    return props.data
  }
  if (props.config?.data) {
    return props.config.data
  }
  return []
})

const chartOption = computed(() => {
  return props.config?.option || undefined
})

// 从config中获取字段映射
const xField = computed(() => props.config?.xField || 'name')
const yField = computed(() => props.config?.yField || 'value')
const title = computed(() => props.config?.title || '')
</script>

<style scoped>
.chart-widget {
  width: 100%;
  height: 100%;
}
</style>
