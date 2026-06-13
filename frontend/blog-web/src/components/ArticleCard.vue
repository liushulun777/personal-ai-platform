<script setup lang="ts">
import { NTag, NImage } from 'naive-ui'
import { useRouter } from 'vue-router'
import type { ArticleVO } from '@/api/article'

const props = defineProps<{
  article: ArticleVO
}>()

const router = useRouter()

function formatDate(date: string | null | undefined) {
  if (!date) return ''
  return date.slice(0, 10)
}

function handleClick() {
  router.push(`/article/${props.article.id}`)
}
</script>

<template>
  <div
    class="rounded-xl overflow-hidden cursor-pointer transition-all duration-200 hover:-translate-y-0.5"
    style="background: var(--bg-card); border: 1px solid var(--border-color); box-shadow: var(--shadow-sm)"
    @click="handleClick"
  >
    <!-- Cover -->
    <div v-if="article.cover" class="aspect-video overflow-hidden">
      <NImage
        :src="article.cover"
        width="100%"
        height="100%"
        object-fit="cover"
        preview-disabled
        class="block"
        fallback-src=""
      />
    </div>
    <div v-else class="aspect-video flex items-center justify-center" style="background: var(--hover-bg)">
      <span class="text-2xl" style="color: var(--text-faint)">{{ article.title[0] }}</span>
    </div>

    <!-- Body -->
    <div class="p-4">
      <!-- Tags -->
      <div v-if="article.tags?.length" class="flex flex-wrap gap-1.5 mb-2">
        <NTag
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          size="tiny"
          :bordered="false"
          round
          :style="{ color: tag.color || 'var(--accent)' }"
        >
          {{ tag.name }}
        </NTag>
      </div>

      <!-- Title -->
      <h3 class="text-base font-semibold leading-snug mb-2 line-clamp-2" style="color: var(--text-primary)">
        {{ article.title }}
      </h3>

      <!-- Summary -->
      <p class="text-xs leading-relaxed mb-3 line-clamp-2" style="color: var(--text-muted)">
        {{ article.summary }}
      </p>

      <!-- Meta -->
      <div class="flex items-center justify-between text-xs" style="color: var(--text-faint)">
        <div class="flex items-center gap-2">
          <span>{{ article.authorName }}</span>
          <span>·</span>
          <span>{{ formatDate(article.publishTime) }}</span>
        </div>
        <div class="flex items-center gap-3">
          <span>{{ article.viewCount }} 阅读</span>
          <span>{{ article.likeCount }} 点赞</span>
        </div>
      </div>
    </div>
  </div>
</template>
