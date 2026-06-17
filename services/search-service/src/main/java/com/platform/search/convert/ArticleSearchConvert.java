package com.platform.search.convert;

import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.domain.vo.ArticleSearchVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 文章搜索转换器
 */
@Mapper(componentModel = "spring")
public interface ArticleSearchConvert {

    /**
     * 文档转VO
     *
     * @param document 文档
     * @return VO
     */
    @Mapping(target = "contentFragment", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "semanticScore", ignore = true)
    ArticleSearchVO documentToVO(ArticleDocument document);

    /**
     * 文档列表转VO列表
     *
     * @param documents 文档列表
     * @return VO列表
     */
    List<ArticleSearchVO> documentListToVOList(List<ArticleDocument> documents);
}
