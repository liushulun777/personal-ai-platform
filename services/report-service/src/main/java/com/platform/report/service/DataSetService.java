package com.platform.report.service;

import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.DataSetCreateDTO;
import com.platform.report.domain.dto.DataSetQueryDTO;
import com.platform.report.domain.dto.DataSetUpdateDTO;
import com.platform.report.domain.vo.DataSetVO;

import java.util.List;
import java.util.Map;

/**
 * 数据集服务接口
 */
public interface DataSetService {

    /**
     * 分页查询数据集
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DataSetVO> page(DataSetQueryDTO queryDTO);

    /**
     * 获取数据集详情
     *
     * @param id 数据集ID
     * @return 数据集详情
     */
    DataSetVO getById(Long id);

    /**
     * 创建数据集
     *
     * @param createDTO 创建数据集DTO
     * @return 数据集ID
     */
    Long create(DataSetCreateDTO createDTO);

    /**
     * 更新数据集
     *
     * @param id 数据集ID
     * @param updateDTO 更新数据集DTO
     */
    void update(Long id, DataSetUpdateDTO updateDTO);

    /**
     * 删除数据集
     *
     * @param id 数据集ID
     */
    void delete(Long id);

    /**
     * 预览数据集数据
     *
     * @param id 数据集ID
     * @param params 查询参数
     * @return 数据列表
     */
    List<Map<String, Object>> preview(Long id, Map<String, Object> params);

    /**
     * 获取数据集字段
     *
     * @param id 数据集ID
     * @return 字段列表
     */
    List<Map<String, Object>> getFields(Long id);
}
