<template>
  <div ref="chartRef" :style="{ width: '100%', height: '100%' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

interface Props {
  option?: echarts.EChartsOption
  data?: any[]
  nameField?: string
  valueField?: string
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  option: undefined,
  data: () => [],
  nameField: 'name',
  valueField: 'value',
  title: ''
})

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const colors = ['#18a058', '#2080f0', '#f0a020', '#e04040', '#9060c0', '#60c0d0']

const getDefaultOption = (): echarts.EChartsOption => {
  const pieData = props.data?.map(item => ({
    name: item[props.nameField],
    value: item[props.valueField]
  })) || []

  return {
    title: props.title ? { text: props.title, left: 'center' } : undefined,
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    color: colors,
    series: [{
      name: props.title || '数据',
      type: 'pie',
      radius: '55%',
      center: ['50%', '60%'],
      data: pieData,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
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
