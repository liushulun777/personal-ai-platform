package com.platform.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.DashboardCreateDTO;
import com.platform.report.domain.dto.DashboardQueryDTO;
import com.platform.report.domain.dto.DashboardUpdateDTO;
import com.platform.report.domain.entity.Dashboard;
import com.platform.report.domain.entity.DataSource;
import com.platform.report.domain.entity.DataSet;
import com.platform.report.domain.vo.DashboardDetailVO;
import com.platform.report.domain.vo.DashboardVO;
import com.platform.report.mapper.DashboardMapper;
import com.platform.report.mapper.DataSourceMapper;
import com.platform.report.mapper.DataSetMapper;
import com.platform.report.service.DashboardService;
import com.platform.report.util.DataSetExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 大屏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final DataSetMapper dataSetMapper;
    private final DataSourceMapper dataSourceMapper;

    @Override
    public PageResult<DashboardVO> page(DashboardQueryDTO queryDTO) {
        LambdaQueryWrapper<Dashboard> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getName()), Dashboard::getName, queryDTO.getName())
                .eq(queryDTO.getStatus() != null, Dashboard::getStatus, queryDTO.getStatus())
                .orderByDesc(Dashboard::getCreateTime);

        Page<Dashboard> page = dashboardMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        return PageResult.of(BeanUtil.copyToList(page.getRecords(), DashboardVO.class), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public DashboardDetailVO getById(Long id) {
        Dashboard dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("大屏不存在");
        }
        return BeanUtil.copyProperties(dashboard, DashboardDetailVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DashboardCreateDTO createDTO) {
        Dashboard dashboard = BeanUtil.copyProperties(createDTO, Dashboard.class);
        dashboard.setStatus(0); // 草稿
        dashboardMapper.insert(dashboard);
        return dashboard.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DashboardUpdateDTO updateDTO) {
        Dashboard dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("大屏不存在");
        }
        BeanUtil.copyProperties(updateDTO, dashboard, "id");
        dashboardMapper.updateById(dashboard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Dashboard dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("大屏不存在");
        }
        dashboardMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String share(Long id) {
        Dashboard dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("大屏不存在");
        }
        // 生成分享链接
        String shareUrl = "dashboard/" + RandomUtil.randomString(16);
        dashboard.setShareUrl(shareUrl);
        dashboard.setStatus(1); // 已发布
        dashboardMapper.updateById(dashboard);
        return shareUrl;
    }

    @Override
    public Map<String, Object> getData(Long id) {
        Dashboard dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("大屏不存在");
        }

        Map<String, Object> result = new HashMap<>();
        Object config = dashboard.getConfig();

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
                    List<Map<String, Object>> data = executeDataset(datasetId);
                    result.put(datasetId, data);
                } catch (Exception e) {
                    log.error("执行数据集查询失败: datasetId={}", datasetId, e);
                    result.put(datasetId, Collections.emptyList());
                }
            }
        } catch (Exception e) {
            log.error("解析大屏配置失败: id={}", id, e);
        }

        return result;
    }

    /**
     * 执行数据集查询
     */
    private List<Map<String, Object>> executeDataset(String datasetId) {
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
                return DataSetExecutor.executeSql(dataSource.getType(), dataSource.getConfig(), sql, null);
            case "API":
                return DataSetExecutor.executeApi(dataSet.getConfig(), null);
            default:
                log.warn("不支持的数据集类型: {}", dataSet.getType());
                return Collections.emptyList();
        }
    }
}
