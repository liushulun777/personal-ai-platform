package com.platform.blog.convert;

import com.platform.blog.domain.dto.CategoryCreateDTO;
import com.platform.blog.domain.entity.Category;
import com.platform.blog.domain.vo.CategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 分类转换器
 */
@Mapper(componentModel = "spring")
public interface CategoryConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Category createDTOToEntity(CategoryCreateDTO dto);

    /**
     * 实体转VO
     */
    CategoryVO entityToVO(Category entity);

    /**
     * 实体列表转VO列表
     */
    List<CategoryVO> entityListToVOList(List<Category> entities);
}
