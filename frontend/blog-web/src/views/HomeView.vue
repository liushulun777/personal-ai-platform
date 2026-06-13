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
  <div class="max-w-6xl mx-auto px-6 py-10">
    <!-- Header -->
    <div class="mb-10">
      <h1 class="text-2xl font-bold mb-2" style="color: var(--text-primary)">最新文章</h1>
      <p class="text-sm" style="color: var(--text-muted)">分享技术见解与实践经验</p>
    </div>

    <NSpin :show="loading">
      <div v-if="articles.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
        />
      </div>
      <div v-else-if="!loading" class="py-20">
        <NEmpty description="暂无文章" />
      </div>
    </NSpin>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="flex justify-center mt-10">
      <NPagination
        :page="current"
        :page-count="Math.ceil(total / pageSize)"
        :page-slot="7"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>
