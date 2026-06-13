package com.platform.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.blog.convert.CategoryConvert;
import com.platform.blog.domain.dto.CategoryCreateDTO;
import com.platform.blog.domain.dto.CategoryUpdateDTO;
import com.platform.blog.domain.entity.Article;
import com.platform.blog.domain.entity.Category;
import com.platform.blog.domain.vo.CategoryVO;
import com.platform.blog.mapper.ArticleMapper;
import com.platform.blog.mapper.CategoryMapper;
import com.platform.blog.service.CategoryService;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;
    private final CategoryConvert categoryConvert;

    @Override
    public List<CategoryVO> listAll() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, CommonConstant.STATUS_ENABLED)
                        .eq(Category::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(Category::getSort)
        );
        return categoryConvert.entityListToVOList(categories);
    }

    @Override
    public CategoryVO getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "分类不存在");
        }
        return categoryConvert.entityToVO(category);
    }

    @Override
    public Long create(CategoryCreateDTO createDTO) {
        // 检查别名是否重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getSlug, createDTO.getSlug())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "分类别名已存在");
        }

        Category category = categoryConvert.createDTOToEntity(createDTO);
        category.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        category.setSort(createDTO.getSort() != null ? createDTO.getSort() : 0);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void update(Long id, CategoryUpdateDTO updateDTO) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "分类不存在");
        }

        // 检查别名是否重复
        if (!category.getSlug().equals(updateDTO.getSlug())) {
            Long count = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getSlug, updateDTO.getSlug())
                            .ne(Category::getId, id)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_DUPLICATE, "分类别名已存在");
            }
        }

        category.setName(updateDTO.getName());
        category.setSlug(updateDTO.getSlug());
        category.setDescription(updateDTO.getDescription());
        category.setSort(updateDTO.getSort());
        category.setStatus(updateDTO.getStatus());
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        // 检查是否有文章使用该分类
        Long articleCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getCategoryId, id)
        );
        if (articleCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该分类下存在文章，不能删除");
        }

        categoryMapper.deleteById(id);
    }
}
