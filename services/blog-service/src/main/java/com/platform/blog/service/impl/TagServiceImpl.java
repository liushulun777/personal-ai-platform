package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.blog.convert.TagConvert;
import com.platform.blog.domain.dto.TagCreateDTO;
import com.platform.blog.domain.dto.TagUpdateDTO;
import com.platform.blog.domain.entity.ArticleTag;
import com.platform.blog.domain.entity.Tag;
import com.platform.blog.domain.vo.TagVO;
import com.platform.blog.mapper.ArticleTagMapper;
import com.platform.blog.mapper.TagMapper;
import com.platform.blog.service.TagService;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签服务实现
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagConvert tagConvert;

    @Override
    public List<TagVO> listAll() {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getStatus, CommonConstant.STATUS_ENABLED)
                        .eq(Tag::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(Tag::getSort)
        );
        return tagConvert.entityListToVOList(tags);
    }

    @Override
    public TagVO getById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "标签不存在");
        }
        return tagConvert.entityToVO(tag);
    }

    @Override
    public Long create(TagCreateDTO createDTO) {
        // 检查别名是否重复
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getSlug, createDTO.getSlug())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "标签别名已存在");
        }

        Tag tag = tagConvert.createDTOToEntity(createDTO);
        tag.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        tag.setSort(createDTO.getSort() != null ? createDTO.getSort() : 0);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void update(Long id, TagUpdateDTO updateDTO) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "标签不存在");
        }

        // 检查别名是否重复
        if (!tag.getSlug().equals(updateDTO.getSlug())) {
            Long count = tagMapper.selectCount(
                    new LambdaQueryWrapper<Tag>()
                            .eq(Tag::getSlug, updateDTO.getSlug())
                            .ne(Tag::getId, id)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_DUPLICATE, "标签别名已存在");
            }
        }

        tag.setName(updateDTO.getName());
        tag.setSlug(updateDTO.getSlug());
        tag.setDescription(updateDTO.getDescription());
        tag.setColor(updateDTO.getColor());
        tag.setSort(updateDTO.getSort());
        tag.setStatus(updateDTO.getStatus());
        tagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        // 检查是否有文章使用该标签
        Long articleCount = articleTagMapper.selectCount(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getTagId, id)
        );
        if (articleCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该标签下存在文章，不能删除");
        }

        tagMapper.deleteById(id);
    }
}
