<script setup lang="ts">
import { NTag, NImage } from 'naive-ui'
import { useRouter } from 'vue-router'
import { Eye, Heart } from 'lucide-vue-next'
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
    class="group rounded-xl overflow-hidden cursor-pointer transition-all duration-300 hover:-translate-y-1"
    style="background: var(--bg-card); border: 1px solid var(--border-color); box-shadow: var(--shadow-sm)"
    @click="handleClick"
  >
    <!-- Cover -->
    <div v-if="article.cover" class="aspect-video overflow-hidden relative">
      <NImage
        :src="article.cover"
        width="100%"
        height="100%"
        object-fit="cover"
        preview-disabled
        class="block transition-transform duration-500 group-hover:scale-105"
        fallback-src=""
      />
      <div
        class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-300"
        style="background: linear-gradient(to top, rgba(0,0,0,0.5) 0%, transparent 60%)"
      />
    </div>
    <div v-else class="aspect-video flex items-center justify-center relative overflow-hidden" style="background: var(--accent-gradient-subtle)">
      <span class="text-4xl font-bold opacity-30" style="color: var(--accent)">{{ article.title[0] }}</span>
      <div
        class="absolute bottom-0 left-0 right-0 h-1/2"
        style="background: linear-gradient(to top, var(--bg-card), transparent)"
      />
    </div>

    <!-- Body -->
    <div class="p-5">
      <!-- Tags -->
      <div v-if="article.tags?.length" class="flex flex-wrap gap-1.5 mb-3">
        <NTag
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          size="tiny"
          :bordered="false"
          round
          :style="{
            background: tag.color ? `${tag.color}20` : 'var(--accent-soft)',
            color: tag.color || 'var(--accent)'
          }"
        >
          {{ tag.name }}
        </NTag>
      </div>

      <!-- Title -->
      <h3
        class="text-base font-semibold leading-snug mb-2 line-clamp-2 transition-colors duration-200"
        style="color: var(--text-primary)"
      >
        {{ article.title }}
      </h3>

      <!-- Summary -->
      <p class="text-xs leading-relaxed mb-4 line-clamp-2" style="color: var(--text-muted)">
        {{ article.summary }}
      </p>

      <!-- Meta -->
      <div class="flex items-center justify-between text-xs" style="color: var(--text-faint)">
        <div class="flex items-center gap-2">
          <span>{{ article.authorName }}</span>
          <span style="color: var(--text-faint)">·</span>
          <span>{{ formatDate(article.publishTime) }}</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="flex items-center gap-1">
            <Eye :size="13" :stroke-width="1.5" />
            {{ article.viewCount }}
          </span>
          <span class="flex items-center gap-1">
            <Heart :size="13" :stroke-width="1.5" />
            {{ article.likeCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
