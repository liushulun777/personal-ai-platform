/**
 * 数据导出工具
 * 支持 CSV 和 Excel 格式导出
 */

/**
 * 导出为 CSV 文件
 */
export function exportToCsv(filename: string, data: Record<string, unknown>[], columns?: string[]) {
  if (!data || data.length === 0) {
    console.warn('No data to export')
    return
  }

  // 获取列名
  const headers = columns || Object.keys(data[0])

  // 构建 CSV 内容
  const csvContent = [
    // 表头
    headers.map(h => `"${h}"`).join(','),
    // 数据行
    ...data.map(row =>
      headers.map(header => {
        const value = row[header]
        if (value === null || value === undefined) return '""'
        // 转义双引号
        const strValue = String(value).replace(/"/g, '""')
        return `"${strValue}"`
      }).join(',')
    )
  ].join('\n')

  // 添加 BOM 支持中文
  const bom = '﻿'
  const blob = new Blob([bom + csvContent], { type: 'text/csv;charset=utf-8;' })

  downloadBlob(blob, `${filename}.csv`)
}

/**
 * 导出为 JSON 文件
 */
export function toJson(filename: string, data: unknown) {
  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: 'application/json' })
  downloadBlob(blob, `${filename}.json`)
}

/**
 * 下载 Blob
 */
function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()

  // 清理
  setTimeout(() => {
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }, 100)
}

/**
 * 格式化日期为文件名友好的格式
 */
export function formatDateForFilename(): string {
  const now = new Date()
  return `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`
}

/**
 * 通用导出函数
 */
export function useExport() {
  const exportCsv = (filename: string, data: Record<string, unknown>[], columns?: string[]) => {
    const fullFilename = `${filename}_${formatDateForFilename()}`
    exportToCsv(fullFilename, data, columns)
  }

  const exportJson = (filename: string, data: unknown) => {
    const fullFilename = `${filename}_${formatDateForFilename()}`
    toJson(fullFilename, data)
  }

  return {
    exportCsv,
    exportJson
  }
}
