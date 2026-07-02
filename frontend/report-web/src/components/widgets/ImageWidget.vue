<template>
  <div class="image-widget">
    <n-image
      v-if="src"
      :src="src"
      :width="'100%'"
      :height="'100%'"
      object-fit="contain"
      :fallback-src="fallbackSrc"
    />
    <div v-else class="placeholder">
      <n-icon size="48" color="#ccc">
        <svg viewBox="0 0 24 24">
          <path fill="currentColor" d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
        </svg>
      </n-icon>
      <span>图片</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  config: any
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  data: undefined
})

const src = computed(() => {
  if (props.config?.src) {
    let url = props.config.src
    // 支持数据绑定
    if (props.data && typeof props.data === 'object') {
      Object.keys(props.data).forEach(key => {
        url = url.replace(`{{${key}}}`, props.data[key] || '')
      })
    }
    return url
  }
  return ''
})

const fallbackSrc = 'data:image/svg+xml,' + encodeURIComponent('<svg width="200" height="200" xmlns="http://www.w3.org/2000/svg"><rect width="200" height="200" fill="#f5f5f5"/><text x="50%" y="50%" font-family="Arial" font-size="14" fill="#ccc" text-anchor="middle" dy=".3em">图片</text></svg>')
</script>

<style scoped>
.image-widget {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #ccc;
}
</style>
