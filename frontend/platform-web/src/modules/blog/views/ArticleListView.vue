<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NTag } from 'naive-ui/es/tag'
import { NSpace } from 'naive-ui/es/space'
import { NPopconfirm } from 'naive-ui/es/popconfirm'
import { NInput } from 'naive-ui/es/input'
import { NSelect } from 'naive-ui/es/select'
import { NEmpty } from 'naive-ui/es/empty'
import { NPagination } from 'naive-ui/es/pagination'
import { NButton, NSpin } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import {
  getArticlePage,
  deleteArticle,
  publishArticle,
  archiveArticle
} from '@/api/article'
import type { ArticleVO, ArticleQueryParams } from '@/api/article'
import ArticleCard from '@/modules/blog/components/ArticleCard.vue'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const viewMode = ref<'card' | 'table'>('card')

const queryParams = ref({
  title: '',
  status: null as number | null
})

const articles = ref<ArticleVO[]>([])
const pagination = ref({
  page: 1,
  pageSize: 12,
  itemCount: 0
})

const statusOptions = [
  { label: '全部', value: null },
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已归档', value: 2 }
]

const statusMap: Record<number, { label: string; type: 'default' | 'warning' | 'success' }> = {
  0: { label: '草稿', type: 'warning' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已归档', type: 'default' }
}

const columns: DataTableColumns<ArticleVO> = [
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '分类', key: 'categoryName', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const status = statusMap[row.status]
      return h(NTag, { type: status.type, size: 'small', bordered: false }, { default: () => status.label })
    }
  },
  { title: '浏览', key: 'viewCount', width: 70 },
  { title: '点赞', key: 'likeCount', width: 70 },
  { title: '发布时间', key: 'publishTime', width: 110 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'info', onClick: () => router.push(`/blog/articles/${row.id}/preview`) }, { default: () => '预览' }),
          h(NButton, { size: 'small', quaternary: true, onClick: () => router.push(`/blog/articles/${row.id}`) }, { default: () => '编辑' }),
          row.status === 0 ? h(NButton, {
            size: 'small', quaternary: true, type: 'success',
            onClick: () => handlePublish(row.id)
          }, { default: () => '发布' }) : null,
          row.status === 1 ? h(NButton, {
            size: 'small', quaternary: true, type: 'warning',
            onClick: () => handleArchive(row.id)
          }, { default: () => '归档' }) : null,
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
            trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, { default: () => '删除' }),
            default: () => '确认删除该文章？'
          })
        ].filter(Boolean)
      })
    }
  }
]

async function loadArticles() {
  loading.value = true
  try {
    const params: ArticleQueryParams = {
      current: pagination.value.page,
      size: pagination.value.pageSize,
      title: queryParams.value.title || undefined,
      status: queryParams.value.status ?? undefined
    }
    const { data } = await getArticlePage(params)
    articles.value = data.data.records
    pagination.value.itemCount = data.data.total
  } catch {
    message.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadArticles()
}

async function handlePublish(id: number) {
  try {
    await publishArticle(id)
    message.success('发布成功')
    loadArticles()
  } catch {
    message.error('发布失败')
  }
}

async function handleArchive(id: number) {
  try {
    await archiveArticle(id)
    message.success('归档成功')
    loadArticles()
  } catch {
    message.error('归档失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deleteArticle(id)
    message.success('删除成功')
    loadArticles()
  } catch {
    message.error('删除失败')
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadArticles()
}

function handleCardClick(id: number) {
  router.push(`/blog/articles/${id}/preview`)
}

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-lg font-semibold" style="color: var(--text-primary)">文章管理</h2>
      <NButton type="primary" size="small" @click="router.push('/blog/articles/new')">
        新建文章
      </NButton>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-3 mb-6">
      <NInput
        v-model:value="queryParams.title"
        placeholder="搜索文章标题..."
        clearable
        size="small"
        class="max-w-xs"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <NSelect
        v-model:value="queryParams.status"
        :options="statusOptions"
        placeholder="状态"
        clearable
        size="small"
        class="w-28"
        @update:value="handleSearch"
      />
      <NButton size="small" @click="handleSearch">搜索</NButton>

      <div class="flex-1" />

      <!-- View toggle -->
      <div class="flex items-center gap-1 border rounded-md overflow-hidden" style="border-color: var(--border-color)">
        <button
          class="px-2.5 py-1 text-xs transition-colors"
          :style="viewMode === 'card' ? 'background: var(--accent-soft); color: var(--text-primary)' : 'color: var(--text-muted)'"
          @click="viewMode = 'card'"
        >
          网格
        </button>
        <button
          class="px-2.5 py-1 text-xs transition-colors"
          :style="viewMode === 'table' ? 'background: var(--accent-soft); color: var(--text-primary)' : 'color: var(--text-muted)'"
          @click="viewMode = 'table'"
        >
          列表
        </button>
      </div>
    </div>

    <!-- Card Grid -->
    <NSpin :show="loading">
      <template v-if="viewMode === 'card'">
        <div v-if="articles.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
            @click="handleCardClick"
          />
        </div>
        <div v-else class="py-20">
          <NEmpty description="暂无文章" />
        </div>
      </template>

      <!-- Table View -->
      <template v-else>
        <div class="border rounded-lg overflow-hidden" style="border-color: var(--border-color)">
          <NDataTable
            :columns="columns"
            :data="articles"
            :bordered="false"
            :single-line="false"
          />
        </div>
      </template>
    </NSpin>

    <!-- Pagination -->
    <div v-if="pagination.itemCount > 0" class="mt-6 flex justify-center">
      <NPagination
        :page="pagination.page"
        :page-size="pagination.pageSize"
        :item-count="pagination.itemCount"
        :page-sizes="[12, 24, 48]"
        show-size-picker
        @update:page="handlePageChange"
        @update:page-size="(size: number) => { pagination.pageSize = size; pagination.page = 1; loadArticles() }"
      />
    </div>
  </div>
</template>
