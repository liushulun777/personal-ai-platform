package com.platform.knowledge.service.impl;

import com.platform.knowledge.service.ChunkingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块服务实现
 */
@Slf4j
@Service
public class ChunkingServiceImpl implements ChunkingService {

    @Override
    public List<String> chunk(String content, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        // 先按段落分割
        String[] paragraphs = content.split("\n\n+");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;

            // 如果当前块加上新段落超过大小限制
            if (currentChunk.length() + trimmed.length() + 1 > chunkSize) {
                // 如果当前块不为空，先保存
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // 保留重叠部分
                    String overlapText = getOverlapText(currentChunk.toString(), overlap);
                    currentChunk = new StringBuilder(overlapText);
                }

                // 如果单个段落超过块大小，按句子进一步分割
                if (trimmed.length() > chunkSize) {
                    List<String> subChunks = chunkBySentence(trimmed, chunkSize, overlap);
                    for (int i = 0; i < subChunks.size() - 1; i++) {
                        chunks.add(subChunks.get(i));
                    }
                    // 最后一个子块留给下一个迭代处理
                    currentChunk = new StringBuilder(subChunks.get(subChunks.size() - 1));
                } else {
                    if (currentChunk.length() > 0) {
                        currentChunk.append("\n\n");
                    }
                    currentChunk.append(trimmed);
                }
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmed);
            }
        }

        // 保存最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 按句子分割长文本
     */
    private List<String> chunkBySentence(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        // 按中英文句号、感叹号、问号分割
        String[] sentences = text.split("(?<=[。！？.!?\\n])");
        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    String overlapText = getOverlapText(currentChunk.toString(), overlap);
                    currentChunk = new StringBuilder(overlapText);
                }
            }
            currentChunk.append(sentence);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 获取文本末尾的重叠部分
     */
    private String getOverlapText(String text, int overlap) {
        if (text.length() <= overlap) {
            return text;
        }
        return text.substring(text.length() - overlap);
    }
}
