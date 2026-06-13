package com.platform.blog.convert;

import com.platform.blog.domain.dto.TagCreateDTO;
import com.platform.blog.domain.entity.Tag;
import com.platform.blog.domain.vo.TagVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 标签转换器
 */
@Mapper(componentModel = "spring")
public interface TagConvert {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Tag createDTOToEntity(TagCreateDTO dto);

    /**
     * 实体转VO
     */
    TagVO entityToVO(Tag entity);

    /**
     * 实体列表转VO列表
     */
    List<TagVO> entityListToVOList(List<Tag> entities);
}
