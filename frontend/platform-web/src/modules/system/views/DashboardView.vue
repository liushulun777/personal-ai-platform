<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NGrid, NGi, NStatistic, NSpin } from 'naive-ui'
import { getBlogStatistics } from '@/api/statistics'
import type { BlogStatisticsVO } from '@/api/statistics'

const loading = ref(false)
const stats = ref<BlogStatisticsVO>({
  articleCount: 0,
  categoryCount: 0,
  tagCount: 0,
  commentCount: 0,
  totalViews: 0,
  totalLikes: 0
})

async function loadStatistics() {
  loading.value = true
  try {
    const { data } = await getBlogStatistics()
    stats.value = data.data
  } catch {
    // error handled
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div>
    <h2 class="text-lg font-semibold mb-6" style="color: var(--text-primary)">仪表盘</h2>

    <NSpin :show="loading">
      <NGrid :cols="3" :x-gap="16" :y-gap="16">
        <NGi>
          <NCard content-style="padding: 20px;">
            <NStatistic label="文章总数" :value="stats.articleCount" />
          </NCard>
        </NGi>
        <NGi>
          <NCard content-style="padding: 20px;">
            <NStatistic label="分类数量" :value="stats.categoryCount" />
          </NCard>
        </NGi>
        <NGi>
          <NCard content-style="padding: 20px;">
            <NStatistic label="标签数量" :value="stats.tagCount" />
          </NCard>
        </NGi>
        <NGi>
          <NCard content-style="padding: 20px;">
            <NStatistic label="评论数量" :value="stats.commentCount" />
          </NCard>
        </NGi>
        <NGi>
          <NCard content-style="padding: 20px;">
            <NStatistic label="总浏览量" :value="stats.totalViews" />
          </NCard>
        </NGi>
        <NGi>
          <NCard content-style="padding: 20px;">
            <NStatistic label="总点赞数" :value="stats.totalLikes" />
          </NCard>
        </NGi>
      </NGrid>
    </NSpin>

    <NCard class="mt-6" content-style="padding: 24px;">
      <template #header>
        <span class="text-sm font-medium" style="color: var(--text-secondary)">欢迎使用 Personal AI Platform</span>
      </template>
      <p class="text-sm leading-relaxed" style="color: var(--text-muted)">
        这是一个长期演进的个人技术中台项目，当前处于 Phase1：博客系统阶段。
      </p>
      <p class="text-sm mt-2" style="color: var(--text-faint)">
        使用左侧菜单开始管理您的内容。
      </p>
    </NCard>
  </div>
</template>
