<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { NSpin, NEmpty, NPagination, NTag, NInput } from 'naive-ui'
import { Eye, Heart } from 'lucide-vue-next'
import { searchArticles } from '@/api/search'
import type { SearchResult, SearchPageResult } from '@/api/search'

const router = useRouter()
const loading = ref(false)
const results = ref<SearchResult[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = 10
const keyword = ref('')

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
}

function goToArticle(id: number) {
  router.push(`/blog/articles/${id}/preview`)
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
</script>

<template>
  <div>
    <h2 class="text-lg font-semibold mb-6">
      <span class="gradient-text">文章搜索</span>
    </h2>

    <div class="mb-6">
      <NInput
        v-model:value="keyword"
        placeholder="输入关键词搜索文章..."
        clearable
        @keydown.enter="current = 1; doSearch()"
        @clear="results = []; total = 0"
      >
        <template #prefix>
          <span style="color: var(--text-muted)">&#128269;</span>
        </template>
      </NInput>
    </div>

    <NSpin :show="loading">
      <div v-if="results.length" class="space-y-3">
        <p class="text-xs mb-4" style="color: var(--text-muted)">
          共找到 <span style="color: var(--accent)">{{ total }}</span> 篇相关文章
        </p>

        <div
          v-for="item in results"
          :key="item.id"
          class="p-4 rounded-lg cursor-pointer transition-all duration-200 hover:-translate-y-0.5"
          style="background: var(--bg-card); border: 1px solid var(--border-color)"
          @click="goToArticle(item.id)"
        >
          <h3
            class="text-sm font-semibold mb-1.5"
            style="color: var(--text-primary)"
            v-html="highlightKeyword(item.title, keyword)"
          />
          <p
            v-if="item.contentFragment"
            class="text-xs leading-relaxed mb-2 line-clamp-2"
            style="color: var(--text-muted)"
            v-html="highlightKeyword(item.contentFragment, keyword)"
          />
          <div v-if="item.tags?.length" class="flex flex-wrap gap-1 mb-2">
            <NTag
              v-for="tag in item.tags.slice(0, 4)"
              :key="tag"
              size="tiny"
              :bordered="false"
              round
              :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
            >
              {{ tag }}
            </NTag>
          </div>
          <div class="flex items-center justify-between text-[11px]" style="color: var(--text-faint)">
            <div class="flex items-center gap-2">
              <span>{{ item.authorName }}</span>
              <span v-if="item.categoryName">· {{ item.categoryName }}</span>
              <span>· {{ formatDate(item.publishTime) }}</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="flex items-center gap-1"><Eye :size="11" :stroke-width="1.5" /> {{ item.viewCount }}</span>
              <span class="flex items-center gap-1"><Heart :size="11" :stroke-width="1.5" /> {{ item.likeCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="!loading && keyword" class="py-16 text-center">
        <NEmpty description="未找到相关文章" />
      </div>
    </NSpin>

    <div v-if="total > pageSize" class="flex justify-center mt-6">
      <NPagination
        :page="current"
        :page-count="Math.ceil(total / pageSize)"
        :page-slot="7"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>
