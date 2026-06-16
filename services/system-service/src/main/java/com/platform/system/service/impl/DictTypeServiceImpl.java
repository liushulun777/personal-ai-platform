package com.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.system.convert.DictTypeConvert;
import com.platform.system.domain.dto.DictTypeCreateDTO;
import com.platform.system.domain.dto.DictTypeQueryDTO;
import com.platform.system.domain.dto.DictTypeUpdateDTO;
import com.platform.system.domain.entity.SysDictData;
import com.platform.system.domain.entity.SysDictType;
import com.platform.system.domain.vo.DictTypeVO;
import com.platform.system.mapper.SysDictDataMapper;
import com.platform.system.mapper.SysDictTypeMapper;
import com.platform.system.service.DictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典类型服务实现
 */
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl implements DictTypeService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;
    private final DictTypeConvert dictTypeConvert;

    @Override
    public PageResult<DictTypeVO> page(DictTypeQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getDictName()), SysDictType::getDictName, queryDTO.getDictName());
        wrapper.eq(StringUtils.hasText(queryDTO.getDictType()), SysDictType::getDictType, queryDTO.getDictType());
        wrapper.eq(queryDTO.getStatus() != null, SysDictType::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(SysDictType::getCreateTime);

        Page<SysDictType> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<SysDictType> result = sysDictTypeMapper.selectPage(page, wrapper);

        List<DictTypeVO> records = dictTypeConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<DictTypeVO> listAll() {
        List<SysDictType> list = sysDictTypeMapper.selectList(
                new LambdaQueryWrapper<SysDictType>()
                        .eq(SysDictType::getStatus, CommonConstant.STATUS_ENABLED)
                        .orderByAsc(SysDictType::getCreateTime)
        );
        return dictTypeConvert.entityListToVOList(list);
    }

    @Override
    public DictTypeVO getById(Long id) {
        SysDictType dictType = sysDictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典类型不存在");
        }
        return dictTypeConvert.entityToVO(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DictTypeCreateDTO createDTO) {
        Long count = sysDictTypeMapper.selectCount(
                new LambdaQueryWrapper<SysDictType>()
                        .eq(SysDictType::getDictType, createDTO.getDictType())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }

        SysDictType dictType = dictTypeConvert.createDTOToEntity(createDTO);
        dictType.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        sysDictTypeMapper.insert(dictType);
        return dictType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DictTypeUpdateDTO updateDTO) {
        SysDictType dictType = sysDictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典类型不存在");
        }

        if (!dictType.getDictType().equals(updateDTO.getDictType())) {
            Long count = sysDictTypeMapper.selectCount(
                    new LambdaQueryWrapper<SysDictType>()
                            .eq(SysDictType::getDictType, updateDTO.getDictType())
                            .ne(SysDictType::getId, id)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.DATA_DUPLICATE, "字典类型已存在");
            }
        }

        dictType.setDictName(updateDTO.getDictName());
        dictType.setDictType(updateDTO.getDictType());
        dictType.setDescription(updateDTO.getDescription());
        dictType.setStatus(updateDTO.getStatus());
        sysDictTypeMapper.updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysDictType dictType = sysDictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典类型不存在");
        }

        Long dataCount = sysDictDataMapper.selectCount(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType.getDictType())
        );
        if (dataCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该字典类型下存在字典数据，不能删除");
        }

        sysDictTypeMapper.deleteById(id);
    }
}
