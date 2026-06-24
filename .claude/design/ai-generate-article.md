# AI 生成文章功能设计文档

## 1. 需求概述

在文章编辑页面（ArticleDetailView.vue）的"文章内容"卡片 header 区域增加 AI 生成文章功能，支持用户通过 AI 快速生成、续写和润色文章内容。

## 2. 功能清单

| 功能 | 说明 | 优先级 |
|------|------|--------|
| AI 生成文章 | 根据标题/主题生成完整 Markdown 文章 | P0 |
| AI 文章续写 | 基于已有内容继续写作 | P0 |
| AI 文章润色 | 优化已有内容的表达 | P1 |
| AI 内容扩展 | 对选中段落进行扩写 | P2 |

## 3. 交互设计

### 3.1 入口位置

在文章内容卡片的 header 右侧增加 AI 操作按钮组：

```vue
<NCard class="mt-4">
  <template #header>
    <div class="flex justify-between items-center">
      <span class="text-sm font-medium" style="color: var(--text-secondary)">文章内容</span>
      <NSpace :size="4">
        <NButton size="tiny" type="primary" ghost @click="showAiGenerateModal = true">
          <template #icon><NIcon><SparklesOutline /></NIcon></template>
          AI 生成
        </NButton>
      </NSpace>
    </div>
  </template>
  <!-- 编辑器 -->
</NCard>
```

### 3.2 生成文章弹窗

点击"AI 生成"按钮后，弹出模态框让用户输入生成参数：

```
┌─────────────────────────────────────────┐
│  AI 生成文章                           │
├─────────────────────────────────────────┤
│                                         │
│  文章主题/标题 *                        │
│  ┌─────────────────────────────────┐    │
│  │ 请输入文章主题或标题             │    │
│  └─────────────────────────────────┘    │
│                                         │
│  文章类型                               │
│  ┌─────────────────────────────────┐    │
│  │ 技术博客 ▼                      │    │
│  └─────────────────────────────────┘    │
│  可选: 技术博客 / 教程 / 总结 / 随笔   │
│                                         │
│  写作风格                               │
│  ┌─────────────────────────────────┐    │
│  │ 专业严谨 ▼                      │    │
│  └─────────────────────────────────┘    │
│  可选: 专业严谨 / 轻松活泼 / 简洁明了  │
│                                         │
│  文章长度                               │
│  ┌─────────────────────────────────┐    │
│  │ 中等 (约1000字) ▼               │    │
│  └─────────────────────────────────┘    │
│  可选: 短(~500) / 中(~1000) / 长(~2000)│
│                                         │
│  补充说明（可选）                       │
│  ┌─────────────────────────────────┐    │
│  │ 如需包含特定要点，请在此说明     │    │
│  └─────────────────────────────────┘    │
│                                         │
│  [取消]              [生成文章]         │
└─────────────────────────────────────────┘
```

### 3.3 生成过程

1. 点击"生成文章"后，按钮显示 loading 状态
2. 使用流式输出（SSE），实时将生成内容写入编辑器
3. 生成过程中显示"停止生成"按钮
4. 生成完成后，内容直接填入 MdEditor

### 3.4 续写/润色

- **续写**: 编辑器 toolbar 区域增加"AI 续写"按钮，基于当前光标位置后的内容继续生成
- **润色**: 选中文本后，右键菜单或 toolbar 按钮触发"AI 润色"

## 4. 技术设计

### 4.1 前端架构

#### 4.1.1 新增组件

**文件**: `frontend/platform-web/src/components/ai/AiArticleGenerateModal.vue`

```vue
<template>
  <NModal v-model:show="visible" preset="card" title="AI 生成文章" style="width: 600px">
    <NForm ref="formRef" :model="formData" label-placement="left" label-width="80">
      <NFormItem label="文章主题" path="topic" :rules="[{ required: true, message: '请输入文章主题' }]">
        <NInput v-model:value="formData.topic" placeholder="请输入文章主题或标题" />
      </NFormItem>

      <NFormItem label="文章类型" path="type">
        <NSelect v-model:value="formData.type" :options="typeOptions" />
      </NFormItem>

      <NFormItem label="写作风格" path="style">
        <NSelect v-model:value="formData.style" :options="styleOptions" />
      </NFormItem>

      <NFormItem label="文章长度" path="length">
        <NSelect v-model:value="formData.length" :options="lengthOptions" />
      </NFormItem>

      <NFormItem label="补充说明" path="extra">
        <NInput v-model:value="formData.extra" type="textarea" :rows="3" placeholder="可选：如需包含特定要点，请在此说明" />
      </NFormItem>
    </NForm>

    <template #action>
      <NSpace justify="end">
        <NButton @click="visible = false">取消</NButton>
        <NButton type="primary" :loading="generating" @click="handleGenerate">
          {{ generating ? '生成中...' : '生成文章' }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
```

**Props & Emits**:
```ts
interface Props {
  currentTitle?: string  // 自动填充当前文章标题
}

interface Emits {
  (e: 'generated', content: string): void  // 生成完成，返回内容
}
```

#### 4.1.2 API 扩展

**文件**: `frontend/platform-web/src/api/ai.ts`

```ts
/** AI 生成文章 */
export function generateArticle(params: {
  topic: string
  type?: string
  style?: string
  length?: string
  extra?: string
}) {
  return request.post<ApiResult<string>>('/ai/generate/article', params)
}

/** AI 生成文章（流式） */
export async function generateArticleStream(
  params: ArticleGenerateParams,
  onChunk: (text: string) => void,
  onDone?: () => void,
  onError?: (error: Error) => void
): Promise<AbortController> {
  // 类似 chatStream 实现
}
```

#### 4.1.3 页面集成

**文件**: `frontend/platform-web/src/modules/blog/views/ArticleDetailView.vue`

```vue
<script setup>
// 新增状态
const showAiGenerateModal = ref(false)

// AI 生成文章回调
function handleArticleGenerated(content: string) {
  formData.value.content = content
  showAiGenerateModal.value = false
}
</script>

<template>
  <!-- 文章内容卡片 header 增加 AI 按钮 -->
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
    <!-- 原有编辑器 -->
  </NCard>

  <!-- 新增弹窗 -->
  <AiArticleGenerateModal
    v-model:show="showAiGenerateModal"
    :current-title="formData.title"
    @generated="handleArticleGenerated"
  />
</template>
```

### 4.2 后端架构

#### 4.2.1 DTO 定义

**文件**: `services/ai-service/src/main/java/com/platform/ai/domain/dto/ArticleGenerateDTO.java`

```java
@Data
public class ArticleGenerateDTO {
    @NotBlank(message = "文章主题不能为空")
    private String topic;

    /** 文章类型: tech_blog, tutorial, summary, essay */
    private String type = "tech_blog";

    /** 写作风格: professional, casual, concise */
    private String style = "professional";

    /** 文章长度: short, medium, long */
    private String length = "medium";

    /** 补充说明 */
    private String extra;
}
```

#### 4.2.2 Controller 扩展

**文件**: `services/ai-service/src/main/java/com/platform/ai/controller/AiGenerateController.java`

```java
/**
 * 生成文章
 */
@Operation(summary = "生成文章", description = "根据主题和参数生成完整文章")
@PostMapping("/article")
public Result<String> generateArticle(@Valid @RequestBody ArticleGenerateDTO dto) {
    return Result.success(aiGenerateService.generateArticle(dto));
}

/**
 * 生成文章（流式）
 */
@Operation(summary = "生成文章（流式）", description = "流式生成文章内容")
@PostMapping(value = "/article/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter generateArticleStream(@Valid @RequestBody ArticleGenerateDTO dto) {
    return aiGenerateService.generateArticleStream(dto);
}
```

#### 4.2.3 Service 扩展

**文件**: `services/ai-service/src/main/java/com/platform/ai/service/AiGenerateService.java`

```java
/**
 * 生成文章
 */
String generateArticle(ArticleGenerateDTO dto);

/**
 * 生成文章（流式）
 */
SseEmitter generateArticleStream(ArticleGenerateDTO dto);
```

**实现** (`AiGenerateServiceImpl.java`):

```java
@Override
public String generateArticle(ArticleGenerateDTO dto) {
    try {
        String prompt = buildArticlePrompt(dto);
        return aiService.chat(prompt);
    } catch (Exception e) {
        log.error("生成文章失败", e);
        throw new BusinessException(ResultCode.FAIL, "生成文章失败: " + e.getMessage());
    }
}

@Override
public SseEmitter generateArticleStream(ArticleGenerateDTO dto) {
    SseEmitter emitter = new SseEmitter(60_000L); // 60秒超时
    String prompt = buildArticlePrompt(dto);

    // 异步执行流式生成
    CompletableFuture.runAsync(() -> {
        try {
            aiService.chatStream(prompt, chunk -> {
                try {
                    emitter.send(SseEmitter.event().data(chunk));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });

    return emitter;
}

private String buildArticlePrompt(ArticleGenerateDTO dto) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("请撰写一篇关于「").append(dto.getTopic()).append("」的文章。\n\n");

    // 文章类型
    switch (dto.getType()) {
        case "tech_blog" -> prompt.append("文章类型：技术博客，包含技术背景、实现细节、代码示例等。\n");
        case "tutorial" -> prompt.append("文章类型：教程，步骤清晰，包含代码示例和说明。\n");
        case "summary" -> prompt.append("文章类型：总结/综述，全面概述相关技术和方案。\n");
        case "essay" -> prompt.append("文章类型：随笔，风格轻松，可以有个人观点。\n");
    }

    // 写作风格
    switch (dto.getStyle()) {
        case "professional" -> prompt.append("写作风格：专业严谨，用词准确。\n");
        case "casual" -> prompt.append("写作风格：轻松活泼，易于阅读。\n");
        case "concise" -> prompt.append("写作风格：简洁明了，直击要点。\n");
    }

    // 文章长度
    switch (dto.getLength()) {
        case "short" -> prompt.append("文章长度：约500字。\n");
        case "medium" -> prompt.append("文章长度：约1000字。\n");
        case "long" -> prompt.append("文章长度：约2000字。\n");
    }

    // 补充说明
    if (dto.getExtra() != null && !dto.getExtra().isEmpty()) {
        prompt.append("\n补充要求：").append(dto.getExtra()).append("\n");
    }

    prompt.append("\n请直接输出 Markdown 格式的文章内容，不要添加任何额外说明。");

    return prompt.toString();
}
```

### 4.3 Prompt 设计

```
请撰写一篇关于「{topic}」的文章。

文章类型：{type_description}
写作风格：{style_description}
文章长度：{length_description}

{extra_requirements}

请直接输出 Markdown 格式的文章内容，要求：
1. 结构清晰，使用合适的标题层级
2. 内容专业、有深度
3. 如为技术文章，包含代码示例
4. 不要添加任何额外说明或前缀
```

## 5. 文件变更清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `frontend/platform-web/src/components/ai/AiArticleGenerateModal.vue` | 新增 | AI 生成文章弹窗组件 |
| `frontend/platform-web/src/api/ai.ts` | 修改 | 增加 generateArticle API |
| `frontend/platform-web/src/modules/blog/views/ArticleDetailView.vue` | 修改 | 集成 AI 生成按钮和弹窗 |
| `services/ai-service/.../domain/dto/ArticleGenerateDTO.java` | 新增 | 生成文章请求 DTO |
| `services/ai-service/.../controller/AiGenerateController.java` | 修改 | 增加生成文章接口 |
| `services/ai-service/.../service/AiGenerateService.java` | 修改 | 增加服务方法定义 |
| `services/ai-service/.../service/impl/AiGenerateServiceImpl.java` | 修改 | 实现生成文章逻辑 |

## 6. 测试要点

1. **前端测试**
   - 弹窗表单验证正常
   - 流式内容实时显示
   - 生成过程中可取消
   - 生成内容正确填入编辑器

2. **后端测试**
   - 不同参数组合生成结果符合预期
   - 流式输出稳定
   - 异常处理正确

## 7. 后续扩展

1. **AI 续写**: 基于光标位置续写
2. **AI 润色**: 选中文本后润色
3. **大纲生成**: 先生成大纲，再按章节生成
4. **多轮对话式生成**: 用户可逐步指导 AI 完善内容
