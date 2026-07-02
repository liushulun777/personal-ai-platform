<template>
  <div ref="chartRef" :style="{ width: '100%', height: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

interface Props {
  option?: echarts.EChartsOption
  value?: number
  title?: string
  min?: number
  max?: number
}

const props = withDefaults(defineProps<Props>(), {
  option: undefined,
  value: 0,
  title: '',
  min: 0,
  max: 100
})

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const getDefaultOption = (): echarts.EChartsOption => {
  return {
    title: props.title ? { text: props.title, left: 'center' } : undefined,
    series: [{
      type: 'gauge',
      min: props.min,
      max: props.max,
      progress: {
        show: true,
        width: 18,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#18a058' },
            { offset: 1, color: '#36ad6a' }
          ])
        }
      },
      axisLine: {
        lineStyle: {
          width: 18,
          color: [[1, '#e6e6e6']]
        }
      },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      pointer: { show: false },
      title: {
        show: true,
        offsetCenter: [0, '70%'],
        fontSize: 14,
        color: '#999'
      },
      detail: {
        valueAnimation: true,
        fontSize: 32,
        offsetCenter: [0, '0%'],
        formatter: '{value}%',
        color: '#333'
      },
      data: [{
        value: props.value,
        name: props.title
      }]
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
watch(() => props.value, updateChart)

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
  chart = null
})
</script>
