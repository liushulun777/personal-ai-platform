<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NSpin, NEmpty, NPagination, NButton } from 'naive-ui'
import { getArticlePage } from '@/api/article'
import type { ArticleVO, ArticleQueryParams } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()
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
    // Filter by tag on frontend since backend doesn't support tag filter yet
    const tagId = Number(route.params.id)
    articles.value = data.data.records.filter(a => a.tags?.some(t => t.id === tagId))
    total.value = articles.value.length
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

watch(() => route.params.id, () => {
  current.value = 1
  loadArticles()
})

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-10">
    <div class="mb-8">
      <NButton quaternary size="small" @click="router.push('/')">
        <template #icon><span style="color: var(--text-muted)">&larr;</span></template>
        <span style="color: var(--text-secondary)">返回首页</span>
      </NButton>
    </div>

    <h1 class="text-2xl font-bold mb-3"><span class="gradient-text">标签文章</span></h1>
    <div class="w-12 h-0.5 rounded-full mb-10" style="background: var(--accent-gradient)" />

    <NSpin :show="loading">
      <div v-if="articles.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
        />
      </div>
      <div v-else-if="!loading" class="py-20">
        <NEmpty description="该标签下暂无文章" />
      </div>
    </NSpin>

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
