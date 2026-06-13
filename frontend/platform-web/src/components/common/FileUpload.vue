<script setup lang="ts">
import { ref } from 'vue'
import { NUpload, NButton, NIcon, useMessage } from 'naive-ui'
import type { UploadFileInfo } from 'naive-ui'
import { uploadFile } from '@/api/file'
import type { FileVO } from '@/api/file'

const props = defineProps<{
  module?: string
  accept?: string
  maxSize?: number
  multiple?: boolean
  maxCount?: number
}>()

const emit = defineEmits<{
  (e: 'change', files: FileVO[]): void
}>()

const message = useMessage()
const loading = ref(false)
const fileList = ref<UploadFileInfo[]>([])
const uploadedFiles = ref<FileVO[]>([])

const module = props.module || 'common'
const maxSize = props.maxSize || 10 * 1024 * 1024 // 默认10MB
const maxCount = props.multiple ? (props.maxCount || 5) : 1

async function handleUpload({ file }: { file: UploadFileInfo }) {
  if (!file.file) return

  // 校验文件大小
  if (file.file.size > maxSize) {
    message.error(`文件大小不能超过${Math.round(maxSize / 1024 / 1024)}MB`)
    return
  }

  loading.value = true
  try {
    const { data } = await uploadFile(file.file, module)
    uploadedFiles.value.push(data.data)
    emit('change', uploadedFiles.value)
    message.success('上传成功')
  } catch (error) {
    message.error('上传失败')
  } finally {
    loading.value = false
  }
}

function handleRemove({ file }: { file: UploadFileInfo }) {
  const index = uploadedFiles.value.findIndex(f => f.originalName === file.name)
  if (index !== -1) {
    uploadedFiles.value.splice(index, 1)
    emit('change', uploadedFiles.value)
  }
}
</script>

<template>
  <NUpload
    v-model:file-list="fileList"
    :accept="accept"
    :multiple="multiple"
    :max="maxCount"
    :custom-request="handleUpload"
    @remove="handleRemove"
  >
    <NButton :loading="loading">
      <template #icon>
        <NIcon>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
        </NIcon>
      </template>
      上传文件
    </NButton>
  </NUpload>
</template>
