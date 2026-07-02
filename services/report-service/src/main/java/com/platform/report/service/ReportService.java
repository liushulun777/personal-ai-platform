package com.platform.report.service;

import com.platform.common.core.result.PageResult;
import com.platform.report.domain.dto.ReportCreateDTO;
import com.platform.report.domain.dto.ReportQueryDTO;
import com.platform.report.domain.dto.ReportUpdateDTO;
import com.platform.report.domain.vo.ReportDetailVO;
import com.platform.report.domain.vo.ReportVO;

import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 分页查询报表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ReportVO> page(ReportQueryDTO queryDTO);

    /**
     * 获取报表详情
     *
     * @param id 报表ID
     * @return 报表详情
     */
    ReportDetailVO getById(Long id);

    /**
     * 创建报表
     *
     * @param createDTO 创建报表DTO
     * @return 报表ID
     */
    Long create(ReportCreateDTO createDTO);

    /**
     * 更新报表
     *
     * @param id 报表ID
     * @param updateDTO 更新报表DTO
     */
    void update(Long id, ReportUpdateDTO updateDTO);

    /**
     * 删除报表
     *
     * @param id 报表ID
     */
    void delete(Long id);

    /**
     * 发布报表
     *
     * @param id 报表ID
     */
    void publish(Long id);

    /**
     * 归档报表
     *
     * @param id 报表ID
     */
    void archive(Long id);

    /**
     * 获取报表数据
     *
     * @param id 报表ID
     * @param params 查询参数
     * @return 报表数据
     */
    Map<String, Object> getData(Long id, Map<String, Object> params);

    /**
     * 导出报表
     *
     * @param id 报表ID
     * @param format 导出格式
     * @param params 查询参数
     * @return 文件路径
     */
    String export(Long id, String format, Map<String, Object> params);
}
