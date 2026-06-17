<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NCard, NGrid, NGi, NStatistic, NTag, NSpin } from 'naive-ui'
import { getBugPage } from '@/api/project'
import type { BugVO } from '@/api/project'

const loading = ref(false)
const bugs = ref<BugVO[]>([])

// 严重程度定义
const severityLevels = [
  { key: 0, label: '轻微', color: '#67C23A', type: 'success' },
  { key: 1, label: '一般', color: '#409EFF', type: 'info' },
  { key: 2, label: '严重', color: '#E6A23C', type: 'warning' },
  { key: 3, label: '致命', color: '#F56C6C', type: 'error' }
]

// 状态定义
const statusLevels = [
  { key: 0, label: '待确认', color: '#909399', type: 'info' },
  { key: 1, label: '已确认', color: '#409EFF', type: 'info' },
  { key: 2, label: '修复中', color: '#E6A23C', type: 'warning' },
  { key: 3, label: '已修复', color: '#67C23A', type: 'success' },
  { key: 4, label: '已关闭', color: '#909399', type: 'default' }
]

// 按严重程度统计
const bugsBySeverity = computed(() => {
  const stats: Record<number, number> = {}
  severityLevels.forEach(level => {
    stats[level.key] = bugs.value.filter(b => b.severity === level.key).length
  })
  return stats
})

// 按状态统计
const bugsByStatus = computed(() => {
  const stats: Record<number, number> = {}
  statusLevels.forEach(level => {
    stats[level.key] = bugs.value.filter(b => b.status === level.key).length
  })
  return stats
})

// 总数
const totalBugs = computed(() => bugs.value.length)

// 待处理数（待确认 + 已确认）
const pendingBugs = computed(() =>
  bugs.value.filter(b => b.status === 0 || b.status === 1).length
)

// 处理中数
const processingBugs = computed(() =>
  bugs.value.filter(b => b.status === 2).length
)

// 已完成数（已修复 + 已关闭）
const completedBugs = computed(() =>
  bugs.value.filter(b => b.status === 3 || b.status === 4).length
)

async function loadBugs() {
  loading.value = true
  try {
    const { data } = await getBugPage({ current: 1, size: 1000 })
    bugs.value = data.data?.records || []
  } catch {
    bugs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBugs()
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">Bug 统计</h2>
    </div>

    <NSpin :show="loading">
      <!-- 概览卡片 -->
      <NGrid :cols="4" :x-gap="16" :y-gap="16" class="mb-6">
        <NGi>
          <NCard>
            <NStatistic label="Bug 总数" :value="totalBugs" />
          </NCard>
        </NGi>
        <NGi>
          <NCard>
            <NStatistic label="待处理" :value="pendingBugs">
              <template #suffix>
                <NTag type="warning" size="small">需关注</NTag>
              </template>
            </NStatistic>
          </NCard>
        </NGi>
        <NGi>
          <NCard>
            <NStatistic label="处理中" :value="processingBugs" />
          </NCard>
        </NGi>
        <NGi>
          <NCard>
            <NStatistic label="已完成" :value="completedBugs" />
          </NCard>
        </NGi>
      </NGrid>

      <!-- 按严重程度统计 -->
      <NCard title="按严重程度分布" class="mb-6">
        <NGrid :cols="4" :x-gap="16">
          <NGi v-for="level in severityLevels" :key="level.key">
            <div class="stat-item">
              <div class="stat-header">
                <NTag :type="level.type as any" size="small">{{ level.label }}</NTag>
                <span class="stat-count">{{ bugsBySeverity[level.key] }}</span>
              </div>
              <div class="stat-bar">
                <div
                  class="stat-bar-fill"
                  :style="{
                    width: totalBugs ? (bugsBySeverity[level.key] / totalBugs * 100) + '%' : '0%',
                    backgroundColor: level.color
                  }"
                ></div>
              </div>
            </div>
          </NGi>
        </NGrid>
      </NCard>

      <!-- 按状态统计 -->
      <NCard title="按状态分布">
        <NGrid :cols="5" :x-gap="16">
          <NGi v-for="level in statusLevels" :key="level.key">
            <div class="stat-item">
              <div class="stat-header">
                <NTag :type="level.type as any" size="small">{{ level.label }}</NTag>
                <span class="stat-count">{{ bugsByStatus[level.key] }}</span>
              </div>
              <div class="stat-bar">
                <div
                  class="stat-bar-fill"
                  :style="{
                    width: totalBugs ? (bugsByStatus[level.key] / totalBugs * 100) + '%' : '0%',
                    backgroundColor: level.color
                  }"
                ></div>
              </div>
            </div>
          </NGi>
        </NGrid>
      </NCard>
    </NSpin>
  </div>
</template>

<style scoped>
.stat-item {
  padding: 8px 0;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.stat-count {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-bar {
  height: 8px;
  background: var(--border-color);
  border-radius: 4px;
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}
</style>
