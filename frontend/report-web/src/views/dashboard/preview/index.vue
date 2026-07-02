<template>
  <div class="dashboard-preview" :style="{ background: canvasConfig.background }">
    <div
      class="canvas"
      :style="{ width: canvasConfig.width + 'px', height: canvasConfig.height + 'px', transform: `scale(${scale})` }"
    >
      <div
        v-for="item in components"
        :key="item.id"
        class="component"
        :style="getComponentStyle(item)"
      >
        <WidgetRenderer :config="item" :data="dashboardData" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import { WidgetRenderer } from '@/components/widgets'

const route = useRoute()
const store = useDashboardStore()

// 画布配置
const canvasConfig = computed(() => store.current?.config?.canvas || {
  width: 1920,
  height: 1080,
  background: '#000000'
})

// 组件列表
const components = computed(() => store.current?.config?.components || [])

// 大屏数据
const dashboardData = computed(() => store.dashboardData)

// 缩放比例
const scale = ref(1)

// 计算缩放比例
const calculateScale = () => {
  const scaleX = window.innerWidth / canvasConfig.value.width
  const scaleY = window.innerHeight / canvasConfig.value.height
  scale.value = Math.min(scaleX, scaleY)
}

// 获取组件样式
const getComponentStyle = (item: any) => ({
  position: 'absolute' as const,
  left: item.x + 'px',
  top: item.y + 'px',
  width: item.width + 'px',
  height: item.height + 'px'
})

// 定时刷新
let refreshTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  const id = route.params.id
  if (id) {
    await store.fetchById(String(id))
    await store.fetchData(String(id))
  }

  calculateScale()
  window.addEventListener('resize', calculateScale)

  // 每30秒刷新数据
  refreshTimer = setInterval(() => {
    if (id) {
      store.fetchData(String(id))
    }
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateScale)
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.dashboard-preview {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.canvas {
  position: relative;
  transform-origin: center center;
}

.component {
  position: absolute;
}
</style>
