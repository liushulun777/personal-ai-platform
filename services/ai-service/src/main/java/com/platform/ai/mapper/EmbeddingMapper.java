package com.platform.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.ai.domain.entity.Embedding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 向量嵌入Mapper
 */
@Mapper
public interface EmbeddingMapper extends BaseMapper<Embedding> {

    /**
     * 使用 pgvector 余弦距离进行相似度搜索
     *
     * @param embeddingStr 向量字符串格式 '[0.1,0.2,...]'
     * @param documentId   文档ID过滤（可选）
     * @param limit        返回数量
     * @return 相似的嵌入列表，包含 similarity 字段
     */
    @Select("<script>" +
            "SELECT e.id, e.chunk_id, e.document_id, e.create_time, " +
            "(1 - (e.embedding <![CDATA[<=>]]> #{embeddingStr}::vector)) as similarity " +
            "FROM kb_embedding e " +
            "<where>" +
            "  <if test='documentId != null'>AND e.document_id = #{documentId}</if>" +
            "</where>" +
            "ORDER BY e.embedding <![CDATA[<=>]]> #{embeddingStr}::vector " +
            "LIMIT #{limit}" +
            "</script>")
    List<Embedding> searchSimilar(@Param("embeddingStr") String embeddingStr,
                                  @Param("documentId") Long documentId,
                                  @Param("limit") Integer limit);
}
