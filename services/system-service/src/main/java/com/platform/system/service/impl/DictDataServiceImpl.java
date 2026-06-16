package com.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.constant.CommonConstant;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.system.convert.DictDataConvert;
import com.platform.system.domain.dto.DictDataCreateDTO;
import com.platform.system.domain.dto.DictDataQueryDTO;
import com.platform.system.domain.dto.DictDataUpdateDTO;
import com.platform.system.domain.entity.SysDictData;
import com.platform.system.domain.vo.DictDataVO;
import com.platform.system.mapper.SysDictDataMapper;
import com.platform.system.service.DictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典数据服务实现
 */
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl implements DictDataService {

    private final SysDictDataMapper sysDictDataMapper;
    private final DictDataConvert dictDataConvert;

    @Override
    public PageResult<DictDataVO> page(DictDataQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(queryDTO.getDictType()), SysDictData::getDictType, queryDTO.getDictType());
        wrapper.like(StringUtils.hasText(queryDTO.getDictLabel()), SysDictData::getDictLabel, queryDTO.getDictLabel());
        wrapper.eq(queryDTO.getStatus() != null, SysDictData::getStatus, queryDTO.getStatus());
        wrapper.orderByAsc(SysDictData::getSort);

        Page<SysDictData> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<SysDictData> result = sysDictDataMapper.selectPage(page, wrapper);

        List<DictDataVO> records = dictDataConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<DictDataVO> listByDictType(String dictType) {
        List<SysDictData> list = sysDictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getStatus, CommonConstant.STATUS_ENABLED)
                        .orderByAsc(SysDictData::getSort)
        );
        return dictDataConvert.entityListToVOList(list);
    }

    @Override
    public DictDataVO getById(Long id) {
        SysDictData dictData = sysDictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典数据不存在");
        }
        return dictDataConvert.entityToVO(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DictDataCreateDTO createDTO) {
        SysDictData dictData = dictDataConvert.createDTOToEntity(createDTO);
        dictData.setSort(createDTO.getSort() != null ? createDTO.getSort() : 0);
        dictData.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : CommonConstant.STATUS_ENABLED);
        sysDictDataMapper.insert(dictData);
        return dictData.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DictDataUpdateDTO updateDTO) {
        SysDictData dictData = sysDictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典数据不存在");
        }

        dictData.setDictType(updateDTO.getDictType());
        dictData.setDictLabel(updateDTO.getDictLabel());
        dictData.setDictValue(updateDTO.getDictValue());
        dictData.setSort(updateDTO.getSort());
        dictData.setStatus(updateDTO.getStatus());
        dictData.setRemark(updateDTO.getRemark());
        sysDictDataMapper.updateById(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysDictData dictData = sysDictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典数据不存在");
        }
        sysDictDataMapper.deleteById(id);
    }
}
