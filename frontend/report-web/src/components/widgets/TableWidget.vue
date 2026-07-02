<template>
  <div class="table-widget">
    <n-data-table
      :columns="columns"
      :data="tableData"
      :bordered="true"
      :single-line="false"
      size="small"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DataTableColumns } from 'naive-ui'

interface Props {
  config: any
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  data: undefined
})

const columns = computed<DataTableColumns<any>>(() => {
  if (props.config?.columns) {
    return props.config.columns.map((col: any) => ({
      title: col.label || col.name,
      key: col.name,
      width: col.width || 120,
      ellipsis: { tooltip: true }
    }))
  }

  // 自动从数据生成列
  if (props.data && props.data.length > 0) {
    const firstRow = props.data[0]
    return Object.keys(firstRow).map(key => ({
      title: key,
      key,
      width: 120,
      ellipsis: { tooltip: true }
    }))
  }

  return []
})

const tableData = computed(() => {
  if (Array.isArray(props.data)) {
    return props.data
  }
  if (props.config?.data) {
    return props.config.data
  }
  return []
})
</script>

<style scoped>
.table-widget {
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>
