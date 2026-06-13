<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NTag, NSpin, NImage, NInput, useMessage } from 'naive-ui'
import { marked } from 'marked'
import { getArticleById } from '@/api/article'
import type { ArticleDetailVO } from '@/api/article'
import { getLikeStatus, toggleLike, cancelLike } from '@/api/like'
import { getFavoriteStatus, toggleFavorite, cancelFavorite } from '@/api/favorite'
import { getCommentsByArticleId, createComment, deleteComment } from '@/api/comment'
import type { CommentVO, CommentCreateParams } from '@/api/comment'
import AiArticleQA from '@/components/ai/AiArticleQA.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const loading = ref(false)
const article = ref<ArticleDetailVO | null>(null)

const articleId = route.params.id as string

// Interaction state
const liked = ref(false)
const favorited = ref(false)
const comments = ref<CommentVO[]>([])
const commentContent = ref('')
const submittingComment = ref(false)

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked.parse(article.value.content) as string
})

async function loadArticle() {
  loading.value = true
  try {
    const { data } = await getArticleById(articleId)
    article.value = data.data
  } catch {
    // error handled
  } finally {
    loading.value = false
  }
}

async function loadInteractionStatus() {
  try {
    const [likeRes, favRes] = await Promise.all([
      getLikeStatus(articleId),
      getFavoriteStatus(articleId)
    ])
    liked.value = likeRes.data.data
    favorited.value = favRes.data.data
  } catch {
    // ignore
  }
}

async function loadComments() {
  try {
    const { data } = await getCommentsByArticleId(articleId)
    comments.value = data.data
  } catch {
    // ignore
  }
}

async function handleToggleLike() {
  try {
    if (liked.value) {
      await cancelLike(articleId)
      liked.value = false
      if (article.value) article.value.likeCount--
    } else {
      await toggleLike(articleId)
      liked.value = true
      if (article.value) article.value.likeCount++
    }
  } catch {
    message.error('操作失败')
  }
}

async function handleToggleFavorite() {
  try {
    if (favorited.value) {
      await cancelFavorite(articleId)
      favorited.value = false
      if (article.value) article.value.favoriteCount--
    } else {
      await toggleFavorite(articleId)
      favorited.value = true
      if (article.value) article.value.favoriteCount++
    }
  } catch {
    message.error('操作失败')
  }
}

async function handleSubmitComment() {
  if (!commentContent.value.trim()) {
    message.warning('请输入评论内容')
    return
  }
  submittingComment.value = true
  try {
    const params: CommentCreateParams = {
      articleId: Number(articleId),
      content: commentContent.value.trim()
    }
    await createComment(params)
    commentContent.value = ''
    message.success('评论成功')
    loadComments()
    if (article.value) article.value.commentCount++
  } catch {
    message.error('评论失败')
  } finally {
    submittingComment.value = false
  }
}

async function handleDeleteComment(id: number) {
  try {
    await deleteComment(id)
    message.success('删除成功')
    loadComments()
    if (article.value) article.value.commentCount--
  } catch {
    message.error('删除失败')
  }
}

function formatDate(date: string | null | undefined) {
  if (!date) return ''
  return date.slice(0, 10)
}

function formatDateTime(date: string | null | undefined) {
  if (!date) return ''
  return date.replace('T', ' ').slice(0, 19)
}

onMounted(() => {
  loadArticle()
  loadInteractionStatus()
  loadComments()
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-10">
    <!-- Back -->
    <div class="mb-8">
      <NButton quaternary size="small" @click="router.push('/')">
        <template #icon><span style="color: var(--text-muted)">&larr;</span></template>
        <span style="color: var(--text-secondary)">返回首页</span>
      </NButton>
    </div>

    <NSpin :show="loading">
      <div v-if="article">
        <!-- Cover -->
        <div v-if="article.cover" class="mb-8 rounded-xl overflow-hidden" style="border: 1px solid var(--border-color)">
          <NImage
            :src="article.cover"
            width="100%"
            object-fit="cover"
            class="block max-h-96"
            fallback-src=""
          />
        </div>

        <!-- Title -->
        <h1 class="text-3xl font-bold leading-tight mb-5" style="color: var(--text-primary)">
          {{ article.title }}
        </h1>

        <!-- Meta -->
        <div class="flex flex-wrap items-center gap-2 mb-6 text-sm" style="color: var(--text-muted)">
          <span class="font-medium" style="color: var(--text-secondary)">{{ article.authorName }}</span>
          <span style="color: var(--text-faint)">·</span>
          <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
          <template v-if="article.categoryName">
            <span style="color: var(--text-faint)">·</span>
            <NTag size="small" :bordered="false" type="info">{{ article.categoryName }}</NTag>
          </template>
          <span style="color: var(--text-faint)">·</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>

        <!-- Tags -->
        <div v-if="article.tags?.length" class="flex flex-wrap gap-2 mb-8">
          <NTag
            v-for="tag in article.tags"
            :key="tag.id"
            size="small"
            :bordered="false"
            round
            :style="{ color: tag.color || 'var(--accent)' }"
          >
            {{ tag.name }}
          </NTag>
        </div>

        <!-- Divider -->
        <div class="mb-8" style="border-bottom: 1px solid var(--border-color)" />

        <!-- Summary -->
        <div
          v-if="article.summary"
          class="text-sm mb-8 px-5 py-4 rounded-lg"
          style="color: var(--text-secondary); background: var(--hover-bg); border-left: 3px solid var(--accent)"
        >
          {{ article.summary }}
        </div>

        <!-- Content -->
        <article class="markdown-body" v-html="renderedContent" />

        <!-- Actions -->
        <div class="mt-10 pt-6 flex items-center gap-4" style="border-top: 1px solid var(--border-color)">
          <NButton
            :type="liked ? 'primary' : 'default'"
            size="small"
            @click="handleToggleLike"
          >
            <template #icon><span>{{ liked ? '&#9829;' : '&#9825;' }}</span></template>
            {{ article.likeCount }} 点赞
          </NButton>
          <NButton
            :type="favorited ? 'warning' : 'default'"
            size="small"
            @click="handleToggleFavorite"
          >
            <template #icon><span>{{ favorited ? '&#9733;' : '&#9734;' }}</span></template>
            {{ article.favoriteCount }} 收藏
          </NButton>
          <span class="text-sm" style="color: var(--text-muted)">{{ article.commentCount }} 评论</span>
        </div>

        <!-- Source -->
        <div
          v-if="article.sourceUrl"
          class="mt-6 pt-6 text-sm"
          style="border-top: 1px solid var(--border-color); color: var(--text-muted)"
        >
          原文链接：
          <a :href="article.sourceUrl" target="_blank" class="hover:underline" style="color: var(--accent)">
            {{ article.sourceUrl }}
          </a>
        </div>

        <!-- AI QA -->
        <AiArticleQA :article-id="articleId" />

        <!-- Comments -->
        <div class="mt-10 pt-6" style="border-top: 1px solid var(--border-color)">
          <h3 class="text-base font-semibold mb-6" style="color: var(--text-primary)">评论 ({{ article.commentCount }})</h3>

          <div class="mb-8">
            <NInput
              v-model:value="commentContent"
              type="textarea"
              placeholder="写下你的评论..."
              :rows="3"
              maxlength="1000"
              show-count
            />
            <div class="flex justify-end mt-3">
              <NButton type="primary" size="small" :loading="submittingComment" @click="handleSubmitComment">
                发表评论
              </NButton>
            </div>
          </div>

          <div v-if="comments.length" class="space-y-4">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="p-4 rounded-lg"
              style="background: var(--hover-bg); border: 1px solid var(--border-color)"
            >
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium" style="color: var(--text-secondary)">
                    {{ comment.nickname || comment.username || '匿名用户' }}
                  </span>
                  <span class="text-xs" style="color: var(--text-faint)">
                    {{ formatDateTime(comment.createTime) }}
                  </span>
                </div>
                <NButton text size="tiny" type="error" @click="handleDeleteComment(comment.id)">
                  删除
                </NButton>
              </div>
              <p class="text-sm leading-relaxed" style="color: var(--text-primary)">{{ comment.content }}</p>
            </div>
          </div>
          <div v-else class="text-center py-8 text-sm" style="color: var(--text-muted)">
            暂无评论，来抢沙发吧
          </div>
        </div>
      </div>

      <div v-else-if="!loading" class="text-center py-20" style="color: var(--text-muted)">
        文章不存在或已被删除
      </div>
    </NSpin>
  </div>
</template>

<style scoped>
.markdown-body {
  font-size: 16px;
  line-height: 1.85;
  color: var(--text-primary);
  word-break: break-word;
}

.markdown-body :deep(h1) {
  font-size: 1.75em;
  font-weight: 700;
  margin: 1.5em 0 0.6em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--border-color);
}

.markdown-body :deep(h2) {
  font-size: 1.4em;
  font-weight: 600;
  margin: 1.3em 0 0.5em;
  padding-bottom: 0.25em;
  border-bottom: 1px solid var(--border-color);
}

.markdown-body :deep(h3) {
  font-size: 1.2em;
  font-weight: 600;
  margin: 1.2em 0 0.4em;
}

.markdown-body :deep(h4) {
  font-size: 1.05em;
  font-weight: 600;
  margin: 1em 0 0.3em;
}

.markdown-body :deep(p) {
  margin: 0.8em 0;
}

.markdown-body :deep(img) {
  border-radius: 8px;
  max-width: 100%;
  margin: 1em 0;
}

.markdown-body :deep(a) {
  color: var(--accent);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(code) {
  background: var(--hover-bg);
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-size: 0.88em;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.markdown-body :deep(pre) {
  background: var(--bg-base);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1em;
  margin: 1em 0;
  overflow-x: auto;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 0.9em;
  line-height: 1.6;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid var(--accent);
  color: var(--text-secondary);
  margin: 1em 0;
  padding: 0.5em 1em;
  background: var(--hover-bg);
  border-radius: 0 6px 6px 0;
}

.markdown-body :deep(blockquote p) {
  margin: 0.3em 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 1.5em;
  margin: 0.8em 0;
}

.markdown-body :deep(li) {
  margin: 0.3em 0;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--border-color);
  padding: 0.6em 1em;
  text-align: left;
}

.markdown-body :deep(th) {
  background: var(--hover-bg);
  font-weight: 600;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--border-color);
  margin: 1.5em 0;
}
</style>
