<template>
  <div ref="chartRef" :style="{ width: '100%', height: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

interface Props {
  option?: echarts.EChartsOption
  data?: any[]
  xField?: string
  yField?: string
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  option: undefined,
  data: () => [],
  xField: 'name',
  yField: 'value',
  title: ''
})

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const getDefaultOption = (): echarts.EChartsOption => {
  const xData = props.data?.map(item => item[props.xField]) || []
  const yData = props.data?.map(item => item[props.yField]) || []

  return {
    title: props.title ? { text: props.title } : undefined,
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: xData
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: yData,
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#18a058' },
          { offset: 1, color: '#36ad6a' }
        ])
      }
    }]
  }
}

const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)
  const option = props.option || getDefaultOption()
  chart.setOption(option)
}

const updateChart = () => {
  if (!chart) return
  const option = props.option || getDefaultOption()
  chart.setOption(option, true)
}

watch(() => props.option, updateChart, { deep: true })
watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
  chart = null
})
</script>
