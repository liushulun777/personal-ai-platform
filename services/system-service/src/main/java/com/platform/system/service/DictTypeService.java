package com.platform.system.service;

import com.platform.common.core.result.PageResult;
import com.platform.system.domain.dto.DictTypeCreateDTO;
import com.platform.system.domain.dto.DictTypeQueryDTO;
import com.platform.system.domain.dto.DictTypeUpdateDTO;
import com.platform.system.domain.vo.DictTypeVO;

import java.util.List;

/**
 * 字典类型服务接口
 */
public interface DictTypeService {

    /**
     * 分页查询字典类型
     */
    PageResult<DictTypeVO> page(DictTypeQueryDTO queryDTO);

    /**
     * 获取所有字典类型列表
     */
    List<DictTypeVO> listAll();

    /**
     * 获取字典类型详情
     */
    DictTypeVO getById(Long id);

    /**
     * 创建字典类型
     */
    Long create(DictTypeCreateDTO createDTO);

    /**
     * 更新字典类型
     */
    void update(Long id, DictTypeUpdateDTO updateDTO);

    /**
     * 删除字典类型
     */
    void delete(Long id);
}
