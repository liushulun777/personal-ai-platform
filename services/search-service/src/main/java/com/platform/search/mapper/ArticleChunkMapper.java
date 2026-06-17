package com.platform.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.search.domain.entity.ArticleChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章分块向量Mapper
 */
@Mapper
public interface ArticleChunkMapper extends BaseMapper<ArticleChunk> {

    /**
     * 使用 pgvector 余弦距离进行相似度搜索
     * 支持多维筛选：作者、分类、标签、日期范围
     *
     * @param embeddingStr 向量字符串格式 '[0.1,0.2,...]'
     * @param authorId     作者ID过滤（可选）
     * @param categoryId   分类ID过滤（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param limit        返回数量
     * @return 相似的分块列表，包含 similarity 字段
     */
    @Select("<script>" +
            "SELECT c.id, c.article_id, c.chunk_index, c.content, c.author_id, " +
            "c.category_id, c.tags, c.publish_time, c.create_time, " +
            "(1 - (c.embedding <![CDATA[<=>]]> #{embeddingStr}::vector)) as similarity " +
            "FROM biz_article_chunk c " +
            "<where>" +
            "  <if test='authorId != null'>AND c.author_id = #{authorId}</if>" +
            "  <if test='categoryId != null'>AND c.category_id = #{categoryId}</if>" +
            "  <if test='startDate != null'>AND c.publish_time <![CDATA[>=]]> #{startDate}</if>" +
            "  <if test='endDate != null'>AND c.publish_time <![CDATA[<=]]> #{endDate}</if>" +
            "</where>" +
            "ORDER BY c.embedding <![CDATA[<=>]]> #{embeddingStr}::vector " +
            "LIMIT #{limit}" +
            "</script>")
    List<ArticleChunk> searchSimilar(@Param("embeddingStr") String embeddingStr,
                                     @Param("authorId") Long authorId,
                                     @Param("categoryId") Long categoryId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("limit") Integer limit);

    /**
     * 根据文章ID删除所有分块
     */
    @Delete("DELETE FROM biz_article_chunk WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Long articleId);
}
