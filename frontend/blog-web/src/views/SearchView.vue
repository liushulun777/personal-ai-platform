<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NSpin, NEmpty, NPagination, NTag } from 'naive-ui'
import { Search, Eye, Heart } from 'lucide-vue-next'
import { searchArticles } from '@/api/search'
import type { SearchResult, SearchPageResult } from '@/api/search'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const results = ref<SearchResult[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = 10
const keyword = ref((route.query.q as string) || '')

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const { data } = await searchArticles({
      keyword: keyword.value.trim(),
      current: current.value,
      size: pageSize,
      sortBy: 'relevance'
    })
    const page: SearchPageResult = data.data
    results.value = page.records
    total.value = page.total
  } catch {
    // error handled
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  current.value = page
  doSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToArticle(id: number) {
  router.push(`/article/${id}`)
}

function formatDate(date: string | null | undefined) {
  if (!date) return ''
  return date.slice(0, 10)
}

function highlightKeyword(text: string, kw: string): string {
  if (!kw || !text) return text
  const escaped = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark style="background:var(--accent-soft);color:var(--accent);padding:0 2px;border-radius:2px">$1</mark>')
}

watch(() => route.query.q, (newQ) => {
  if (newQ) {
    keyword.value = newQ as string
    current.value = 1
    doSearch()
  }
})

onMounted(() => {
  if (keyword.value) doSearch()
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-10 animate-fade-in">
    <!-- Search header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold mb-3">
        <span class="gradient-text">搜索文章</span>
      </h1>
      <div class="w-12 h-0.5 rounded-full mb-4" style="background: var(--accent-gradient)" />

      <div
        class="flex items-center gap-3 px-4 py-3 rounded-xl"
        style="background: var(--bg-card); border: 1px solid var(--border-color)"
      >
        <Search :size="18" style="color: var(--text-muted)" />
        <input
          v-model="keyword"
          class="flex-1 bg-transparent border-none outline-none text-sm"
          style="color: var(--text-primary)"
          placeholder="输入关键词搜索..."
          @keydown.enter="current = 1; doSearch()"
        />
      </div>
      <p v-if="keyword" class="text-xs mt-3" style="color: var(--text-muted)">
        共找到 <span style="color: var(--accent)">{{ total }}</span> 篇相关文章
      </p>
    </div>

    <NSpin :show="loading">
      <div v-if="results.length" class="space-y-4">
        <div
          v-for="item in results"
          :key="item.id"
          class="p-5 rounded-xl cursor-pointer transition-all duration-200 hover:-translate-y-0.5"
          style="background: var(--bg-card); border: 1px solid var(--border-color)"
          @click="goToArticle(item.id)"
        >
          <!-- Title -->
          <h3
            class="text-base font-semibold mb-2 leading-snug"
            style="color: var(--text-primary)"
            v-html="highlightKeyword(item.title, keyword)"
          />

          <!-- Content fragment -->
          <p
            v-if="item.contentFragment"
            class="text-sm leading-relaxed mb-3 line-clamp-3"
            style="color: var(--text-muted)"
            v-html="highlightKeyword(item.contentFragment, keyword)"
          />

          <!-- Tags -->
          <div v-if="item.tags?.length" class="flex flex-wrap gap-1.5 mb-3">
            <NTag
              v-for="tag in item.tags.slice(0, 5)"
              :key="tag"
              size="tiny"
              :bordered="false"
              round
              :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
            >
              {{ tag }}
            </NTag>
          </div>

          <!-- Meta -->
          <div class="flex items-center justify-between text-xs" style="color: var(--text-faint)">
            <div class="flex items-center gap-2">
              <span>{{ item.authorName }}</span>
              <span v-if="item.categoryName">· {{ item.categoryName }}</span>
              <span>· {{ formatDate(item.publishTime) }}</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="flex items-center gap-1">
                <Eye :size="12" :stroke-width="1.5" />
                {{ item.viewCount }}
              </span>
              <span class="flex items-center gap-1">
                <Heart :size="12" :stroke-width="1.5" />
                {{ item.likeCount }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="!loading && keyword" class="py-20 text-center">
        <NEmpty description="未找到相关文章" />
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
