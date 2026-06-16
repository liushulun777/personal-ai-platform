package com.platform.project.service;

import com.platform.common.core.result.PageResult;
import com.platform.project.domain.dto.BugCreateDTO;
import com.platform.project.domain.dto.BugQueryDTO;
import com.platform.project.domain.dto.BugUpdateDTO;
import com.platform.project.domain.vo.BugVO;

/**
 * Bug服务接口
 */
public interface BugService {

    /**
     * 分页查询Bug
     */
    PageResult<BugVO> page(BugQueryDTO queryDTO);

    /**
     * 获取Bug详情
     */
    BugVO getById(Long id);

    /**
     * 创建Bug
     */
    Long create(BugCreateDTO dto);

    /**
     * 更新Bug
     */
    void update(BugUpdateDTO dto);

    /**
     * 删除Bug
     */
    void delete(Long id);
}
