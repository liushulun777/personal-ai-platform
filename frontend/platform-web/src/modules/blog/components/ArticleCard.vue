<script setup lang="ts">
import { NTag, NImage } from 'naive-ui'
import type { ArticleVO } from '@/api/article'

defineProps<{
  article: ArticleVO
}>()

const emit = defineEmits<{
  click: [id: number]
}>()

const statusMap: Record<number, { label: string; type: 'warning' | 'success' | 'default' }> = {
  0: { label: '草稿', type: 'warning' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已归档', type: 'default' }
}

function formatDate(date: string | null | undefined) {
  if (!date) return ''
  return date.slice(0, 10)
}
</script>

<template>
  <div
    class="group rounded-lg border overflow-hidden cursor-pointer transition-all duration-200 hover:-translate-y-0.5"
    style="border-color: var(--border-color); background: var(--bg-card);"
    @click="emit('click', article.id)"
  >
    <div
      class="aspect-video overflow-hidden flex items-center justify-center transition-colors"
      style="background: var(--hover-bg)"
    >
      <NImage
        v-if="article.cover"
        :src="article.cover"
        width="100%"
        height="100%"
        object-fit="cover"
        preview-disabled
        class="transition-transform duration-300 group-hover:scale-105"
        fallback-src=""
      />
      <div v-else class="text-3xl font-light" style="color: var(--text-faint)">
        {{ article.title?.[0] || 'A' }}
      </div>
    </div>

    <div class="p-4">
      <div class="flex items-center gap-2 mb-2">
        <NTag
          v-if="statusMap[article.status]"
          :type="statusMap[article.status].type"
          size="tiny"
          :bordered="false"
        >
          {{ statusMap[article.status].label }}
        </NTag>
        <NTag v-if="article.categoryName" size="tiny" :bordered="false" type="info">
          {{ article.categoryName }}
        </NTag>
      </div>

      <h3
        class="text-sm font-medium line-clamp-2 mb-2 group-hover:text-blue-500 transition-colors"
        style="color: var(--text-primary)"
      >
        {{ article.title }}
      </h3>

      <p class="text-xs line-clamp-2 mb-3" style="color: var(--text-muted)">
        {{ article.summary || '暂无摘要' }}
      </p>

      <div v-if="article.tags?.length" class="flex flex-wrap gap-1 mb-3">
        <span
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          class="text-[10px] px-1.5 py-0.5 rounded"
          style="background: var(--hover-bg); color: var(--text-muted)"
        >
          {{ tag.name }}
        </span>
      </div>

      <div class="flex items-center justify-between text-[11px]" style="color: var(--text-faint)">
        <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
        <div class="flex items-center gap-3">
          <span>{{ article.viewCount }} 阅读</span>
          <span>{{ article.likeCount }} 赞</span>
        </div>
      </div>
    </div>
  </div>
</template>
