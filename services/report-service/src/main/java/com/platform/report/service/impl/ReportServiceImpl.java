package com.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.ReportCreateDTO;
import com.platform.report.domain.dto.ReportQueryDTO;
import com.platform.report.domain.dto.ReportUpdateDTO;
import com.platform.report.domain.entity.DataSource;
import com.platform.report.domain.entity.DataSet;
import com.platform.report.domain.entity.ReportDefinition;
import com.platform.report.domain.vo.ReportDetailVO;
import com.platform.report.domain.vo.ReportVO;
import com.platform.report.mapper.DataSourceMapper;
import com.platform.report.mapper.DataSetMapper;
import com.platform.report.mapper.ReportDefinitionMapper;
import com.platform.report.service.ReportService;
import com.platform.report.util.DataSetExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 报表服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportDefinitionMapper reportDefinitionMapper;
    private final DataSetMapper dataSetMapper;
    private final DataSourceMapper dataSourceMapper;

    @Override
    public PageResult<ReportVO> page(ReportQueryDTO queryDTO) {
        LambdaQueryWrapper<ReportDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getName()), ReportDefinition::getName, queryDTO.getName())
                .eq(StrUtil.isNotBlank(queryDTO.getType()), ReportDefinition::getType, queryDTO.getType())
                .eq(StrUtil.isNotBlank(queryDTO.getCategory()), ReportDefinition::getCategory, queryDTO.getCategory())
                .eq(queryDTO.getStatus() != null, ReportDefinition::getStatus, queryDTO.getStatus())
                .orderByDesc(ReportDefinition::getCreateTime);

        Page<ReportDefinition> page = reportDefinitionMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        return PageResult.of(BeanUtil.copyToList(page.getRecords(), ReportVO.class), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ReportDetailVO getById(Long id) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        return BeanUtil.copyProperties(report, ReportDetailVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ReportCreateDTO createDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<ReportDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportDefinition::getCode, createDTO.getCode());
        if (reportDefinitionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("报表编码已存在");
        }

        ReportDefinition report = BeanUtil.copyProperties(createDTO, ReportDefinition.class);
        report.setStatus(0); // 草稿
        report.setVersion(1);
        reportDefinitionMapper.insert(report);
        return report.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ReportUpdateDTO updateDTO) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        BeanUtil.copyProperties(updateDTO, report, "id");
        reportDefinitionMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        reportDefinitionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        report.setStatus(1); // 已发布
        reportDefinitionMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long id) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        report.setStatus(2); // 已归档
        reportDefinitionMapper.updateById(report);
    }

    @Override
    public Map<String, Object> getData(Long id, Map<String, Object> params) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }

        Map<String, Object> result = new HashMap<>();
        Object config = report.getConfig();

        if (config == null) {
            return result;
        }

        try {
            JSONObject configJson = JSONUtil.parseObj(config.toString());
            JSONArray components = configJson.getJSONArray("components");

            if (components == null || components.isEmpty()) {
                return result;
            }

            // 收集所有数据集ID
            Set<String> datasetIds = new HashSet<>();
            for (int i = 0; i < components.size(); i++) {
                JSONObject component = components.getJSONObject(i);
                JSONObject componentConfig = component.getJSONObject("config");
                if (componentConfig != null) {
                    String datasetId = componentConfig.getStr("datasetId");
                    if (StrUtil.isNotBlank(datasetId)) {
                        datasetIds.add(datasetId);
                    }
                }
            }

            // 执行每个数据集的查询
            for (String datasetId : datasetIds) {
                try {
                    List<Map<String, Object>> data = executeDataset(datasetId, params);
                    result.put(datasetId, data);
                } catch (Exception e) {
                    log.error("执行数据集查询失败: datasetId={}", datasetId, e);
                    result.put(datasetId, Collections.emptyList());
                }
            }
        } catch (Exception e) {
            log.error("解析报表配置失败: id={}", id, e);
        }

        return result;
    }

    /**
     * 执行数据集查询
     */
    private List<Map<String, Object>> executeDataset(String datasetId, Map<String, Object> params) {
        DataSet dataSet = dataSetMapper.selectById(Long.parseLong(datasetId));
        if (dataSet == null) {
            log.warn("数据集不存在: {}", datasetId);
            return Collections.emptyList();
        }

        DataSource dataSource = dataSourceMapper.selectById(dataSet.getSourceId());
        if (dataSource == null) {
            log.warn("数据源不存在: sourceId={}", dataSet.getSourceId());
            return Collections.emptyList();
        }

        JSONObject configJson = JSONUtil.parseObj(dataSet.getConfig());

        switch (dataSet.getType().toUpperCase()) {
            case "SQL":
                String sql = configJson.getStr("sql", "");
                return DataSetExecutor.executeSql(dataSource.getType(), dataSource.getConfig(), sql, params);
            case "API":
                return DataSetExecutor.executeApi(dataSet.getConfig(), params);
            default:
                log.warn("不支持的数据集类型: {}", dataSet.getType());
                return Collections.emptyList();
        }
    }

    @Override
    public String export(Long id, String format, Map<String, Object> params) {
        ReportDefinition report = reportDefinitionMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        // TODO: 实现报表导出逻辑
        log.info("导出报表: id={}, format={}, params={}", id, format, params);
        return null;
    }
}
