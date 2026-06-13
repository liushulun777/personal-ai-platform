package com.platform.blog.convert;

import com.platform.blog.domain.dto.ArticleCreateDTO;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.vo.ArticleDetailVO;
import com.platform.blog.domain.vo.ArticleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 文章转换器
 */
@Mapper(componentModel = "spring")
public interface ArticleConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "publishTime", ignore = true)
    Article createDTOToEntity(ArticleCreateDTO dto);

    /**
     * 实体转列表VO
     */
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "tags", ignore = true)
    ArticleVO entityToVO(Article entity);

    /**
     * 实体列表转VO列表
     */
    List<ArticleVO> entityListToVOList(List<Article> entities);

    /**
     * 实体转详情VO
     */
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "tagIds", ignore = true)
    ArticleDetailVO entityToDetailVO(Article entity);
}
