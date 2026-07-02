package com.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.DataSetCreateDTO;
import com.platform.report.domain.dto.DataSetQueryDTO;
import com.platform.report.domain.dto.DataSetUpdateDTO;
import com.platform.report.domain.entity.DataSource;
import com.platform.report.domain.entity.DataSet;
import com.platform.report.domain.vo.DataSetVO;
import com.platform.report.mapper.DataSourceMapper;
import com.platform.report.mapper.DataSetMapper;
import com.platform.report.service.DataSetService;
import com.platform.report.util.DataSetExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 数据集服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSetServiceImpl implements DataSetService {

    private final DataSetMapper dataSetMapper;
    private final DataSourceMapper dataSourceMapper;

    @Override
    public PageResult<DataSetVO> page(DataSetQueryDTO queryDTO) {
        LambdaQueryWrapper<DataSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getName()), DataSet::getName, queryDTO.getName())
                .eq(StrUtil.isNotBlank(queryDTO.getType()), DataSet::getType, queryDTO.getType())
                .eq(queryDTO.getSourceId() != null, DataSet::getSourceId, queryDTO.getSourceId())
                .eq(queryDTO.getStatus() != null, DataSet::getStatus, queryDTO.getStatus())
                .orderByDesc(DataSet::getCreateTime);

        Page<DataSet> page = dataSetMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        List<DataSetVO> voList = BeanUtil.copyToList(page.getRecords(), DataSetVO.class);
        // 填充数据源名称
        voList.forEach(vo -> {
            DataSource dataSource = dataSourceMapper.selectById(vo.getSourceId());
            if (dataSource != null) {
                vo.setSourceName(dataSource.getName());
            }
        });

        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public DataSetVO getById(Long id) {
        DataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null) {
            throw new BusinessException("数据集不存在");
        }
        DataSetVO vo = BeanUtil.copyProperties(dataSet, DataSetVO.class);
        // 填充数据源名称
        DataSource dataSource = dataSourceMapper.selectById(dataSet.getSourceId());
        if (dataSource != null) {
            vo.setSourceName(dataSource.getName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DataSetCreateDTO createDTO) {
        // 验证数据源是否存在
        DataSource dataSource = dataSourceMapper.selectById(createDTO.getSourceId());
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        DataSet dataSet = BeanUtil.copyProperties(createDTO, DataSet.class);
        dataSet.setStatus(1);
        dataSetMapper.insert(dataSet);
        return dataSet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DataSetUpdateDTO updateDTO) {
        DataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null) {
            throw new BusinessException("数据集不存在");
        }
        BeanUtil.copyProperties(updateDTO, dataSet, "id");
        dataSetMapper.updateById(dataSet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null) {
            throw new BusinessException("数据集不存在");
        }
        dataSetMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> preview(Long id, Map<String, Object> params) {
        DataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null) {
            throw new BusinessException("数据集不存在");
        }

        DataSource dataSource = dataSourceMapper.selectById(dataSet.getSourceId());
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        log.info("预览数据集: id={}, type={}", id, dataSet.getType());

        try {
            JSONObject configJson = JSONUtil.parseObj(dataSet.getConfig());

            switch (dataSet.getType().toUpperCase()) {
                case "SQL":
                    String sql = configJson.getStr("sql", "");
                    return DataSetExecutor.executeSql(dataSource.getType(), dataSource.getConfig(), sql, params);
                case "API":
                    return DataSetExecutor.executeApi(dataSet.getConfig(), params);
                default:
                    throw new BusinessException("不支持的数据集类型: " + dataSet.getType());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("数据集预览失败: id={}", id, e);
            throw new BusinessException("数据集预览失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getFields(Long id) {
        DataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null) {
            throw new BusinessException("数据集不存在");
        }

        DataSource dataSource = dataSourceMapper.selectById(dataSet.getSourceId());
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        log.info("获取数据集字段: id={}", id);

        try {
            JSONObject configJson = JSONUtil.parseObj(dataSet.getConfig());

            switch (dataSet.getType().toUpperCase()) {
                case "SQL":
                    String sql = configJson.getStr("sql", "");
                    return DataSetExecutor.getSqlFields(dataSource.getType(), dataSource.getConfig(), sql);
                case "API":
                    // API类型返回空，需要手动配置字段
                    return List.of();
                default:
                    throw new BusinessException("不支持的数据集类型: " + dataSet.getType());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取数据集字段失败: id={}", id, e);
            throw new BusinessException("获取数据集字段失败: " + e.getMessage());
        }
    }
}
