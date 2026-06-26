package com.platform.search.convert;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.search.domain.entity.ArticleDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 文章事件转换器
 * 接收 Kafka 原始 JSON 字符串，解析并转换为 ArticleDocument
 */
@Slf4j
@Component
public class ArticleEventConvert {

    private final ObjectMapper objectMapper;

    public ArticleEventConvert() {
        this.objectMapper = new ObjectMapper();
        // 配置更大的字符串长度限制（500KB，支持长文章）
        this.objectMapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder().maxStringLength(512 * 1024).build()
        );
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * JSON 字符串 → ArticleDocument
     */
    @SuppressWarnings("unchecked")
    public ArticleDocument parseToDocument(String json) {
        if (json == null || json.isEmpty()) return null;

        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return mapToDocument(map);
        } catch (Exception e) {
            log.error("JSON 解析失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Map → ArticleDocument
     */
    @SuppressWarnings("unchecked")
    public ArticleDocument mapToDocument(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;

        ArticleDocument doc = new ArticleDocument();

        doc.setId(toLong(map.getOrDefault("id", map.get("articleId"))));
        doc.setTitle(str(map.get("title")));
        doc.setSummary(str(map.get("summary")));
        doc.setContent(str(map.get("content")));
        doc.setAuthorId(toLong(map.get("authorId")));
        doc.setAuthorName(str(map.get("authorName")));
        doc.setCategoryId(toLong(map.get("categoryId")));
        doc.setCategoryName(str(map.get("categoryName")));
        doc.setStatus(toInt(map.get("status")));
        doc.setViewCount(toInt(map.get("viewCount")));
        doc.setLikeCount(toInt(map.get("likeCount")));
        doc.setFavoriteCount(toInt(map.get("favoriteCount")));
        doc.setCommentCount(toInt(map.get("commentCount")));
        doc.setIsTop(toInt(map.get("isTop")));

        Object tags = map.get("tags");
        doc.setTags(tags instanceof List ? (List<String>) tags : Collections.emptyList());

        doc.setPublishTime(toLdt(map.get("publishTime")));
        doc.setCreateTime(toLdt(map.get("createTime")));
        doc.setUpdateTime(toLdt(map.get("updateTime")));

        return doc;
    }

    // ===== 类型转换 =====

    private String str(Object v) { return v == null ? null : v.toString(); }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private java.time.LocalDateTime toLdt(Object v) {
        if (v == null) return null;
        if (v instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) v;
        if (v instanceof Number) {
            return java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(((Number) v).longValue()),
                    java.time.ZoneId.systemDefault());
        }
        try { return java.time.LocalDateTime.parse(v.toString(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME); } catch (Exception e) { return null; }
    }
}
