import request from '@/utils/request'

/** AI 文章问答 */
export function askArticleQuestion(articleId: string, question: string) {
  return request.post('/ai/generate/ask', {
    articleId: Number(articleId),
    question,
  })
}
