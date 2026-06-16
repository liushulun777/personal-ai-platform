package com.platform.system.service;

import com.platform.common.core.result.PageResult;
import com.platform.system.domain.dto.LogQueryDTO;
import com.platform.system.domain.vo.LogVO;

/**
 * 操作日志服务接口
 */
public interface LogService {

    /**
     * 分页查询操作日志
     */
    PageResult<LogVO> page(LogQueryDTO queryDTO);

    /**
     * 获取操作日志详情
     */
    LogVO getById(Long id);

    /**
     * 删除操作日志
     */
    void delete(Long id);
}
