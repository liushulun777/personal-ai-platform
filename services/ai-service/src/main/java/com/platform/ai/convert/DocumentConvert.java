package com.platform.ai.convert;

import com.platform.ai.domain.entity.Document;
import com.platform.ai.domain.vo.DocumentDetailVO;
import com.platform.ai.domain.vo.DocumentVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 文档转换器
 */
@Mapper(componentModel = "spring")
public interface DocumentConvert {

    /**
     * 实体转VO
     */
    DocumentVO entityToVO(Document entity);

    /**
     * 实体列表转VO列表
     */
    List<DocumentVO> entityListToVOList(List<Document> entities);

    /**
     * 实体转详情VO
     */
    DocumentDetailVO entityToDetailVO(Document entity);
}
