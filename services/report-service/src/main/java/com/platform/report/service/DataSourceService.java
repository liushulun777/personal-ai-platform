package com.platform.report.service;

import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.DataSourceCreateDTO;
import com.platform.report.domain.dto.DataSourceQueryDTO;
import com.platform.report.domain.dto.DataSourceUpdateDTO;
import com.platform.report.domain.vo.DataSourceVO;

/**
 * 数据源服务接口
 */
public interface DataSourceService {

    /**
     * 分页查询数据源
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DataSourceVO> page(DataSourceQueryDTO queryDTO);

    /**
     * 获取数据源详情
     *
     * @param id 数据源ID
     * @return 数据源详情
     */
    DataSourceVO getById(Long id);

    /**
     * 创建数据源
     *
     * @param createDTO 创建数据源DTO
     * @return 数据源ID
     */
    Long create(DataSourceCreateDTO createDTO);

    /**
     * 更新数据源
     *
     * @param id 数据源ID
     * @param updateDTO 更新数据源DTO
     */
    void update(Long id, DataSourceUpdateDTO updateDTO);

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     */
    void delete(Long id);

    /**
     * 测试数据源连接
     *
     * @param id 数据源ID
     * @return 是否连接成功
     */
    boolean testConnection(Long id);
}
