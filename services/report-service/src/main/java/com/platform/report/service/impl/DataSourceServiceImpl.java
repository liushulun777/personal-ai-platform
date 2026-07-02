package com.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.DataSourceCreateDTO;
import com.platform.report.domain.dto.DataSourceQueryDTO;
import com.platform.report.domain.dto.DataSourceUpdateDTO;
import com.platform.report.domain.entity.DataSource;
import com.platform.report.domain.vo.DataSourceVO;
import com.platform.report.mapper.DataSourceMapper;
import com.platform.report.service.DataSourceService;
import com.platform.report.util.DataSourceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceMapper dataSourceMapper;

    @Override
    public PageResult<DataSourceVO> page(DataSourceQueryDTO queryDTO) {
        LambdaQueryWrapper<DataSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getName()), DataSource::getName, queryDTO.getName())
                .eq(StrUtil.isNotBlank(queryDTO.getType()), DataSource::getType, queryDTO.getType())
                .eq(queryDTO.getStatus() != null, DataSource::getStatus, queryDTO.getStatus())
                .orderByDesc(DataSource::getCreateTime);

        Page<DataSource> page = dataSourceMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        return PageResult.of(BeanUtil.copyToList(page.getRecords(), DataSourceVO.class), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public DataSourceVO getById(Long id) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }
        return BeanUtil.copyProperties(dataSource, DataSourceVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DataSourceCreateDTO createDTO) {
        DataSource dataSource = BeanUtil.copyProperties(createDTO, DataSource.class);
        dataSource.setStatus(1);
        dataSourceMapper.insert(dataSource);
        return dataSource.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DataSourceUpdateDTO updateDTO) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }
        BeanUtil.copyProperties(updateDTO, dataSource, "id");
        dataSourceMapper.updateById(dataSource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }
        dataSourceMapper.deleteById(id);
    }

    @Override
    public boolean testConnection(Long id) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }
        log.info("测试数据源连接: id={}, type={}", id, dataSource.getType());
        return DataSourceUtil.testConnection(dataSource.getType(), dataSource.getConfig());
    }
}
