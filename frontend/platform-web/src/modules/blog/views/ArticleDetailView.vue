<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NSwitch,
  NSpace,
  NSpin,
  NImage,
  NDrawer,
  NDrawerContent,
  NIcon,
  useMessage
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { SparklesOutline } from '@vicons/ionicons5'
import FileUpload from '@/components/common/FileUpload.vue'
import AiGenerateButton from '@/components/ai/AiGenerateButton.vue'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'
import AiArticleGenerateModal from '@/components/ai/AiArticleGenerateModal.vue'
import { MdEditor } from 'md-editor-v3'
import type { ToolbarNames } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { uploadFile } from '@/api/file'
import {
  getArticleById,
  createArticle,
  updateArticle,
  publishArticle
} from '@/api/article'
import type { ArticleCreateParams } from '@/api/article'
import { getAllCategories } from '@/api/category'
import { getAllTags } from '@/api/tag'
import type { CategoryVO } from '@/api/category'
import type { TagVO } from '@/api/tag'
import { generateSummary, generateTags, generateTitles } from '@/api/ai'
import { createTag } from '@/api/tag'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const saving = ref(false)
const showChatDrawer = ref(false)
const showAiGenerateModal = ref(false)

// AI 生成函数
function handleArticleGenerated(content: string) {
  formData.value.content = content
  showAiGenerateModal.value = false
}

function handleGenerateSummary() {
  if (!formData.value.content?.trim()) {
    message.warning('请先输入文章内容')
    return Promise.reject(new Error('内容为空'))
  }
  return generateSummary(formData.value.content, 200).then(res => [res.data.data])
}

function handleGenerateTags() {
  if (!formData.value.title?.trim() || !formData.value.content?.trim()) {
    message.warning('请先输入标题和内容')
    return Promise.reject(new Error('标题或内容为空'))
  }
  return generateTags(formData.value.title, formData.value.content, 5).then(res => res.data.data)
}

function handleGenerateTitles() {
  if (!formData.value.content?.trim()) {
    message.warning('请先输入文章内容')
    return Promise.reject(new Error('内容为空'))
  }
  return generateTitles(formData.value.content, 3).then(res => res.data.data)
}

// 待创建的新标签（临时 ID < 0）
let tagIdCounter = 0

// 添加 AI 推荐的标签
function handleAddAiTag(tagName: string) {
  const existing = tagOptions.value.find(t => t.label === tagName)
  if (existing) {
    if (!formData.value.tagIds?.includes(existing.value)) {
      formData.value.tagIds = [...(formData.value.tagIds || []), existing.value]
    }
  } else {
    // 以临时 ID 添加到选项和选中
    const tempId = --tagIdCounter
    tagOptions.value.push({ label: tagName, value: tempId })
    formData.value.tagIds = [...(formData.value.tagIds || []), tempId]
  }
}

// NSelect @create 同步回调：仅返回临时选项，不调用 API
function handleCreateTag(label: string) {
  const trimmed = label.trim()
  if (!trimmed) return { label, value: -1 }

  // 检查是否已存在（忽略大小写）
  const existing = tagOptions.value.find(t => t.label.toLowerCase() === trimmed.toLowerCase())
  if (existing) return existing

  const tempId = --tagIdCounter
  const option = { label: trimmed, value: tempId }
  tagOptions.value.push(option)
  return option
}

// 提交前批量创建新标签，将临时 ID 替换为真实 ID
async function flushNewTags() {
  const newTags = tagOptions.value.filter(t => t.value < 0)
  if (newTags.length === 0) return

  for (const tag of newTags) {
    const { data } = await createTag({
      name: tag.label,
      slug: tag.label.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9一-龥-]/g, ''),
    })
    const realId = data.data
    // 替换选项中的临时 ID
    tag.value = realId
    // 替换 formData 中的临时 ID
    formData.value.tagIds = (formData.value.tagIds || []).map(id => id < 0 ? realId : id)
  }
}

const isCreate = computed(() => route.params.id === 'new')
const articleId = computed(() => (isCreate.value ? null : route.params.id as string))

// Form data
const formData = ref<ArticleCreateParams>({
  title: '',
  summary: '',
  content: '',
  cover: '',
  categoryId: undefined,
  tagIds: [],
  isTop: 0,
  isOriginal: 1,
  sourceUrl: ''
})

// Dropdown options
const categoryOptions = ref<{ label: string; value: number }[]>([])
const tagOptions = ref<{ label: string; value: number }[]>([])

// Form rules
const rules: FormRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }]
}

// Editor toolbar config
const toolbars: ToolbarNames[] = [
  'bold', 'underline', 'italic', 'strikeThrough', '-',
  'title', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', 'task', '-',
  'codeRow', 'code', 'link', 'image', 'table', 'mermaid', 'katex', '-',
  'revoke', 'next', 'save', '=', 'pageFullscreen', 'fullscreen', 'preview', 'htmlPreview', 'catalog'
]

// Handle image upload in editor (supports both click upload and paste)
async function onUploadImg(files: File[], callback: (urls: string[]) => void) {
  const uploadedUrls: string[] = []

  for (const file of files) {
    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      message.error(`图片 ${file.name} 大小不能超过10MB`)
      continue
    }

    // Validate file type
    if (!file.type.startsWith('image/')) {
      message.error(`${file.name} 不是图片文件`)
      continue
    }

    try {
      const { data } = await uploadFile(file, 'blog')
      uploadedUrls.push(data.data.fileUrl)
    } catch {
      message.error(`图片 ${file.name} 上传失败`)
    }
  }

  callback(uploadedUrls)
}

// Load categories and tags
async function loadOptions() {
  try {
    const [catRes, tagRes] = await Promise.all([
      getAllCategories(),
      getAllTags()
    ])
    categoryOptions.value = catRes.data.data.map((c: CategoryVO) => ({
      label: c.name,
      value: c.id
    }))
    tagOptions.value = tagRes.data.data.map((t: TagVO) => ({
      label: t.name,
      value: t.id
    }))
  } catch (error) {
    message.error('加载分类和标签失败，请刷新重试')
  }
}

// Load article for edit mode
async function loadArticle() {
  if (!articleId.value) return
  loading.value = true
  try {
    const { data } = await getArticleById(articleId.value)
    const article = data.data
    formData.value = {
      title: article.title,
      summary: article.summary || '',
      content: article.content || '',
      cover: article.cover || '',
      categoryId: article.categoryId,
      tagIds: article.tagIds || [],
      isTop: article.isTop ?? 0,
      isOriginal: article.isOriginal ?? 1,
      sourceUrl: article.sourceUrl || ''
    }
  } catch (error) {
    message.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

// 保存前校验并创建新标签
async function prepareForSave() {
  try {
    await formRef.value?.validate()
  } catch {
    return false
  }

  if (!formData.value.title.trim()) {
    message.warning('请输入文章标题')
    return false
  }

  saving.value = true
  try {
    await flushNewTags()
    return true
  } catch (error: any) {
    message.error('创建标签失败: ' + (error.message || '未知错误'))
    return false
  }
}

// Save as draft
async function handleSave() {
  if (!(await prepareForSave())) return

  try {
    if (isCreate.value) {
      await createArticle(formData.value)
      message.success('文章创建成功')
    } else {
      await updateArticle(articleId.value!, formData.value)
      message.success('文章更新成功')
    }
    router.push('/blog/articles')
  } catch (error: any) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// Save and publish
async function handlePublish() {
  if (!(await prepareForSave())) return

  try {
    let id = articleId.value
    if (isCreate.value) {
      const { data } = await createArticle(formData.value)
      id = data.data
    } else {
      await updateArticle(articleId.value!, formData.value)
    }
    await publishArticle(id!)
    message.success('文章发布成功')
    router.push('/blog/articles')
  } catch (error: any) {
    message.error(error.message || '发布失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  if (!isCreate.value) {
    await loadArticle()
  }
})
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <NButton quaternary size="small" @click="router.push('/blog/articles')">
        <template #icon><span style="color: var(--text-muted)">&larr;</span></template>
        <span style="color: var(--text-secondary)">返回列表</span>
      </NButton>
      <NSpace :size="8">
        <NButton
          v-if="!isCreate"
          quaternary
          size="small"
          @click="router.push(`/blog/articles/${articleId}/preview`)"
        >
          预览
        </NButton>
        <NButton size="small" quaternary @click="showChatDrawer = true">
          AI 助手
        </NButton>
        <NButton size="small" :loading="saving" @click="handleSave">保存草稿</NButton>
        <NButton type="primary" size="small" :loading="saving" @click="handlePublish">
          {{ isCreate ? '保存并发布' : '发布' }}
        </NButton>
      </NSpace>
    </div>

    <NSpin :show="loading">
      <NForm
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="80"
      >
        <NCard>
          <template #header><span class="text-sm font-medium" style="color: var(--text-secondary)">基本信息</span></template>
          <NFormItem label="标题" path="title">
            <div class="flex gap-2 w-full">
              <NInput
                v-model:value="formData.title"
                placeholder="请输入文章标题"
                maxlength="200"
                show-count
              />
              <AiGenerateButton
                label="AI 生成标题"
                :disabled="!formData.content?.trim()"
                :generate-fn="handleGenerateTitles"
                @use="(v: string) => formData.title = v"
              />
            </div>
          </NFormItem>

          <NFormItem label="分类" path="categoryId">
            <NSelect
              v-model:value="formData.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
              clearable
            />
          </NFormItem>

          <NFormItem label="标签" path="tagIds">
            <div class="flex gap-2 w-full">
              <NSelect
                v-model:value="formData.tagIds"
                :options="tagOptions"
                placeholder="请选择或输入标签"
                multiple
                filterable
                tag
                clearable
                @create="handleCreateTag"
              />
              <AiGenerateButton
                label="AI 推荐标签"
                :disabled="!formData.title.trim() || !formData.content?.trim()"
                :generate-fn="handleGenerateTags"
                @use="handleAddAiTag"
              />
            </div>
          </NFormItem>

          <NFormItem label="摘要" path="summary">
            <div class="flex gap-2 w-full">
              <NInput
                v-model:value="formData.summary"
                type="textarea"
                placeholder="请输入文章摘要"
                maxlength="500"
                show-count
                :rows="3"
              />
              <AiGenerateButton
                label="AI 生成摘要"
                :disabled="!formData.content?.trim()"
                :generate-fn="handleGenerateSummary"
                @use="(v: string) => formData.summary = v"
              />
            </div>
          </NFormItem>

          <NFormItem label="封面" path="cover">
            <div class="w-full">
              <FileUpload
                module="blog"
                accept="image/*"
                :max-size="10 * 1024 * 1024"
                :multiple="false"
                @change="(files: any[]) => { if (files.length) formData.cover = files[0].fileUrl }"
              />
              <NImage
                v-if="formData.cover"
                :src="formData.cover"
                width="200"
                class="mt-2 rounded"
                fallback-src=""
              />
            </div>
          </NFormItem>

          <NFormItem label="置顶" path="isTop">
            <NSwitch
              :value="formData.isTop === 1"
              @update:value="(v: boolean) => (formData.isTop = v ? 1 : 0)"
            />
          </NFormItem>

          <NFormItem label="原创" path="isOriginal">
            <NSwitch
              :value="formData.isOriginal === 1"
              @update:value="(v: boolean) => (formData.isOriginal = v ? 1 : 0)"
            />
          </NFormItem>

          <NFormItem v-if="formData.isOriginal === 0" label="来源" path="sourceUrl">
            <NInput
              v-model:value="formData.sourceUrl"
              placeholder="请输入原文链接"
            />
          </NFormItem>
        </NCard>

        <NCard class="mt-4">
          <template #header>
            <div class="flex justify-between items-center">
              <span class="text-sm font-medium" style="color: var(--text-secondary)">文章内容</span>
              <NButton size="tiny" type="primary" ghost @click="showAiGenerateModal = true">
                <template #icon><NIcon><SparklesOutline /></NIcon></template>
                AI 生成
              </NButton>
            </div>
          </template>
          <NFormItem path="content" :show-label="false">
            <MdEditor
              v-model="formData.content"
              :toolbars="toolbars"
              :on-upload-img="onUploadImg"
              style="height: 500px"
            />
          </NFormItem>
        </NCard>
      </NForm>
    </NSpin>

    <!-- AI 助手抽屉 -->
    <NDrawer
      v-model:show="showChatDrawer"
      :width="400"
      placement="right"
    >
      <NDrawerContent title="AI 助手" :native-scrollbar="false">
        <AiChatPanel />
      </NDrawerContent>
    </NDrawer>

    <!-- AI 生成文章弹窗 -->
    <AiArticleGenerateModal
      v-model:show="showAiGenerateModal"
      :current-title="formData.title"
      @generated="handleArticleGenerated"
    />
  </div>
</template>
