package com.platform.report.service;

import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.DashboardCreateDTO;
import com.platform.report.domain.dto.DashboardQueryDTO;
import com.platform.report.domain.dto.DashboardUpdateDTO;
import com.platform.report.domain.vo.DashboardDetailVO;
import com.platform.report.domain.vo.DashboardVO;

import java.util.Map;

/**
 * 大屏服务接口
 */
public interface DashboardService {

    /**
     * 分页查询大屏
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DashboardVO> page(DashboardQueryDTO queryDTO);

    /**
     * 获取大屏详情
     *
     * @param id 大屏ID
     * @return 大屏详情
     */
    DashboardDetailVO getById(Long id);

    /**
     * 创建大屏
     *
     * @param createDTO 创建大屏DTO
     * @return 大屏ID
     */
    Long create(DashboardCreateDTO createDTO);

    /**
     * 更新大屏
     *
     * @param id 大屏ID
     * @param updateDTO 更新大屏DTO
     */
    void update(Long id, DashboardUpdateDTO updateDTO);

    /**
     * 删除大屏
     *
     * @param id 大屏ID
     */
    void delete(Long id);

    /**
     * 分享大屏
     *
     * @param id 大屏ID
     * @return 分享链接
     */
    String share(Long id);

    /**
     * 获取大屏数据
     *
     * @param id 大屏ID
     * @return 大屏数据
     */
    Map<String, Object> getData(Long id);
}
