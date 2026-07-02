<template>
  <div class="text-widget" :style="style">
    {{ content }}
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

const content = computed(() => {
  if (props.config?.content) {
    // 支持数据绑定
    let text = props.config.content
    if (props.data && typeof props.data === 'object') {
      Object.keys(props.data).forEach(key => {
        text = text.replace(`{{${key}}}`, props.data[key] || '')
      })
    }
    return text
  }
  return props.config?.text || '文本'
})

const style = computed(() => ({
  fontSize: (props.config?.fontSize || 14) + 'px',
  fontWeight: props.config?.fontWeight || 'normal',
  color: props.config?.color || '#333',
  textAlign: props.config?.textAlign || 'left',
  lineHeight: props.config?.lineHeight || 1.5
}))
</script>

<style scoped>
.text-widget {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  word-break: break-all;
}
</style>
