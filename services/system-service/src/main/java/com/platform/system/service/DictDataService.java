package com.platform.system.service;

import com.platform.common.core.result.PageResult;
import com.platform.system.domain.dto.DictDataCreateDTO;
import com.platform.system.domain.dto.DictDataQueryDTO;
import com.platform.system.domain.dto.DictDataUpdateDTO;
import com.platform.system.domain.vo.DictDataVO;

import java.util.List;

/**
 * 字典数据服务接口
 */
public interface DictDataService {

    /**
     * 分页查询字典数据
     */
    PageResult<DictDataVO> page(DictDataQueryDTO queryDTO);

    /**
     * 根据字典类型获取字典数据列表
     */
    List<DictDataVO> listByDictType(String dictType);

    /**
     * 获取字典数据详情
     */
    DictDataVO getById(Long id);

    /**
     * 创建字典数据
     */
    Long create(DictDataCreateDTO createDTO);

    /**
     * 更新字典数据
     */
    void update(Long id, DictDataUpdateDTO updateDTO);

    /**
     * 删除字典数据
     */
    void delete(Long id);
}
