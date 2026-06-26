<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NSpin, NEmpty, NPagination } from 'naive-ui'
import { getArticlePage } from '@/api/article'
import type { ArticleVO, ArticleQueryParams } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = 9

async function loadArticles() {
  loading.value = true
  try {
    const params: ArticleQueryParams = {
      current: current.value,
      size: pageSize,
      status: 1
    }
    const { data } = await getArticlePage(params)
    articles.value = data.data.records
    total.value = data.data.total
  } catch {
    // error handled
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  current.value = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative overflow-hidden py-20 px-6">
      <!-- Decorative background -->
      <div class="absolute inset-0 pointer-events-none">
        <div
          class="absolute top-[-30%] left-[-10%] w-[500px] h-[500px] rounded-full opacity-20"
          style="background: radial-gradient(circle, var(--accent) 0%, transparent 70%); filter: blur(80px)"
        />
        <div
          class="absolute bottom-[-20%] right-[-5%] w-[400px] h-[400px] rounded-full opacity-15"
          style="background: radial-gradient(circle, var(--accent-secondary) 0%, transparent 70%); filter: blur(80px)"
        />
      </div>

      <div class="relative max-w-6xl mx-auto text-center animate-fade-in">
        <h1 class="text-4xl md:text-5xl font-extrabold mb-4 leading-tight">
          <span class="gradient-text">探索技术世界</span>
        </h1>
        <p class="text-base md:text-lg max-w-xl mx-auto leading-relaxed" style="color: var(--text-muted)">
          分享技术见解与实践经验，记录成长的每一步
        </p>
        <!-- Decorative line -->
        <div class="mt-8 mx-auto w-20 h-0.5 rounded-full" style="background: var(--accent-gradient)" />
      </div>
    </section>

    <!-- Articles -->
    <div class="max-w-6xl mx-auto px-6 pb-16">
      <div class="mb-10 animate-fade-in-delay-1">
        <h2 class="text-xl font-bold mb-1" style="color: var(--text-primary)">最新文章</h2>
        <div class="w-12 h-0.5 rounded-full mt-2" style="background: var(--accent-gradient)" />
      </div>

      <NSpin :show="loading">
        <div v-if="articles.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <ArticleCard
            v-for="(article, index) in articles"
            :key="article.id"
            :article="article"
            :style="{ animationDelay: `${index * 0.06}s` }"
            class="animate-fade-in"
          />
        </div>
        <div v-else-if="!loading" class="py-20">
          <NEmpty description="暂无文章" />
        </div>
      </NSpin>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="flex justify-center mt-12">
        <NPagination
          :page="current"
          :page-count="Math.ceil(total / pageSize)"
          :page-slot="7"
          @update:page="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>
