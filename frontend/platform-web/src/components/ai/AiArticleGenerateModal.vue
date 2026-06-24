<template>
  <NModal
    :show="show"
    preset="card"
    title="AI 生成文章"
    style="width: 600px"
    :mask-closable="!generating"
    :closable="!generating"
    @update:show="(val: boolean) => !val && handleClose()"
  >
    <NForm ref="formRef" :model="formData" label-placement="left" label-width="80">
      <NFormItem label="文章主题" path="topic" :rules="[{ required: true, message: '请输入文章主题', trigger: 'blur' }]">
        <NInput v-model:value="formData.topic" placeholder="请输入文章主题或标题" :disabled="generating" />
      </NFormItem>

      <NFormItem label="文章类型" path="type">
        <NSelect
          v-model:value="formData.type"
          :options="typeOptions"
          :disabled="generating"
        />
      </NFormItem>

      <NFormItem label="写作风格" path="style">
        <NSelect
          v-model:value="formData.style"
          :options="styleOptions"
          :disabled="generating"
        />
      </NFormItem>

      <NFormItem label="文章长度" path="length">
        <NSelect
          v-model:value="formData.length"
          :options="lengthOptions"
          :disabled="generating"
        />
      </NFormItem>

      <NFormItem label="补充说明" path="extra">
        <NInput
          v-model:value="formData.extra"
          type="textarea"
          :rows="3"
          placeholder="可选：如需包含特定要点，请在此说明"
          :disabled="generating"
        />
      </NFormItem>
    </NForm>

    <!-- 生成进度 -->
    <div v-if="generating" class="generate-progress">
      <NSpin size="small" />
      <span class="progress-text">AI 正在生成文章... ({{ generatedLength }} 字)</span>
      <NButton size="tiny" type="warning" @click="handleStop">停止生成</NButton>
    </div>

    <template #action>
      <NSpace justify="end">
        <NButton @click="handleClose" :disabled="generating">取消</NButton>
        <NButton type="primary" :loading="generating" @click="handleGenerate">
          {{ generating ? '生成中...' : '生成文章' }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, shallowRef } from 'vue'
import { useMessage } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { generateArticleStream } from '@/api/ai'

interface Props {
  show: boolean
  currentTitle?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'generated', content: string): void
}>()

const message = useMessage()
const formRef = ref<FormInst | null>(null)
const generating = ref(false)
const generatedLength = ref(0)
const abortController = shallowRef<AbortController | null>(null)
const stoppedByUser = ref(false)

const formData = reactive({
  topic: '',
  type: 'tech_blog',
  style: 'professional',
  length: 'medium',
  extra: ''
})

const typeOptions = [
  { label: '技术博客', value: 'tech_blog' },
  { label: '教程', value: 'tutorial' },
  { label: '总结/综述', value: 'summary' },
  { label: '随笔', value: 'essay' }
]

const styleOptions = [
  { label: '专业严谨', value: 'professional' },
  { label: '轻松活泼', value: 'casual' },
  { label: '简洁明了', value: 'concise' }
]

const lengthOptions = [
  { label: '短 (~500字)', value: 'short' },
  { label: '中 (~1000字)', value: 'medium' },
  { label: '长 (~2000字)', value: 'long' }
]

// 监听 show 变化，自动填充标题并重置状态
watch(() => props.show, (val) => {
  if (val) {
    // 打开弹窗时：填充标题、重置生成状态
    if (props.currentTitle) {
      formData.topic = props.currentTitle
    }
    generating.value = false
    generatedLength.value = 0
    abortController.value = null
    stoppedByUser.value = false
  }
})

async function handleGenerate() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  if (!formData.topic.trim()) {
    message.warning('请输入文章主题')
    return
  }

  generating.value = true
  generatedLength.value = 0
  stoppedByUser.value = false
  let fullContent = ''

  abortController.value = await generateArticleStream(
    {
      topic: formData.topic,
      type: formData.type,
      style: formData.style,
      length: formData.length,
      extra: formData.extra || undefined
    },
    // onChunk
    (text: string) => {
      // 过滤结束标记，用 trim 避免空格/换行干扰
      if (text.trim() === '[DONE]') return
      fullContent += text
      generatedLength.value = fullContent.length
    },
    // onDone - 正常完成或用户停止(AbortError)都会走这里
    () => {
      generating.value = false
      if (fullContent) {
        emit('generated', fullContent)
        emit('update:show', false)
        if (!stoppedByUser.value) {
          message.success('文章生成完成')
        } else {
          message.info('已保留已生成的内容')
        }
      }
    },
    // onError - 网络错误等异常情况
    (error: Error) => {
      generating.value = false
      message.error('生成失败: ' + (error.message || '未知错误'))
      // 异常时也保留已生成内容
      if (fullContent) {
        message.info('已保留已生成的内容')
        emit('generated', fullContent)
        emit('update:show', false)
      }
    }
  )
}

function handleStop() {
  stoppedByUser.value = true
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
  generating.value = false
}

function handleClose() {
  if (generating.value) {
    handleStop()
  }
  emit('update:show', false)
}
</script>

<style scoped>
.generate-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--hover-bg);
  border-radius: 6px;
  margin-top: 12px;
}

.progress-text {
  flex: 1;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
