<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {NGrid, NGi, NSpin } from 'naive-ui'
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

interface StatCard {
  label: string
  key: keyof BlogStatisticsVO
  icon: string
  gradient: string
  glow: string
}

const statCards: StatCard[] = [
  { label: '文章总数', key: 'articleCount', icon: '&#128196;', gradient: 'linear-gradient(135deg, #6366f1 0%, #818cf8 100%)', glow: 'rgba(99,102,241,0.2)' },
  { label: '分类数量', key: 'categoryCount', icon: '&#128193;', gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)', glow: 'rgba(139,92,246,0.2)' },
  { label: '标签数量', key: 'tagCount', icon: '&#127991;', gradient: 'linear-gradient(135deg, #ec4899 0%, #f472b6 100%)', glow: 'rgba(236,72,153,0.2)' },
  { label: '评论数量', key: 'commentCount', icon: '&#128172;', gradient: 'linear-gradient(135deg, #14b8a6 0%, #2dd4bf 100%)', glow: 'rgba(20,184,166,0.2)' },
  { label: '总浏览量', key: 'totalViews', icon: '&#128065;', gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)', glow: 'rgba(245,158,11,0.2)' },
  { label: '总点赞数', key: 'totalLikes', icon: '&#9829;', gradient: 'linear-gradient(135deg, #ef4444 0%, #f87171 100%)', glow: 'rgba(239,68,68,0.2)' }
]

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
  <div class="animate-fade-in">
    <!-- Welcome Section -->
    <div
      class="mb-8 p-6 rounded-2xl relative overflow-hidden"
      style="background: var(--accent-gradient-subtle); border: 1px solid var(--border-color)"
    >
      <!-- Decorative elements -->
      <div
        class="absolute -top-10 -right-10 w-40 h-40 rounded-full opacity-20"
        style="background: var(--accent-gradient); filter: blur(40px)"
      />
      <div
        class="absolute -bottom-8 -left-8 w-32 h-32 rounded-full opacity-15"
        style="background: var(--accent-gradient); filter: blur(30px)"
      />

      <div class="relative">
        <h2 class="text-xl font-extrabold mb-2">
          <span class="gradient-text">仪表盘</span>
        </h2>
        <p class="text-sm" style="color: var(--text-muted)">
          欢迎使用 Personal AI Platform，这里是您的数据概览。
        </p>
      </div>
    </div>

    <NSpin :show="loading">
      <!-- Stat Cards -->
      <NGrid :cols="3" :x-gap="16" :y-gap="16">
        <NGi v-for="card in statCards" :key="card.key">
          <div
            class="group rounded-xl p-5 transition-all duration-300 hover:-translate-y-0.5 cursor-default"
            :style="{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-color)',
              boxShadow: 'var(--shadow-sm)'
            }"
            @mouseenter="($event.currentTarget as HTMLElement).style.boxShadow = `0 4px 20px ${card.glow}`"
            @mouseleave="($event.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-sm)'"
          >
            <div class="flex items-center justify-between mb-3">
              <span class="text-2xl" v-html="card.icon" />
              <div
                class="w-10 h-10 rounded-lg flex items-center justify-center text-white text-lg font-bold"
                :style="{ background: card.gradient, boxShadow: `0 4px 12px ${card.glow}` }"
              >
                <span v-html="card.icon" />
              </div>
            </div>
            <div class="text-2xl font-extrabold mb-1" style="color: var(--text-primary)">
              {{ stats[card.key] }}
            </div>
            <div class="text-xs font-medium" style="color: var(--text-muted)">
              {{ card.label }}
            </div>
          </div>
        </NGi>
      </NGrid>
    </NSpin>

    <!-- Info Card -->
    <div
      class="mt-8 p-6 rounded-2xl"
      style="background: var(--bg-card); border: 1px solid var(--border-color)"
    >
      <h3 class="text-sm font-bold mb-3 gradient-text">关于平台</h3>
      <p class="text-sm leading-relaxed" style="color: var(--text-muted)">
        这是一个长期演进的个人技术中台项目，集成博客管理、知识库、AI 助手、项目管理等模块。
        使用左侧菜单开始管理您的内容。
      </p>
    </div>
  </div>
</template>
