package com.platform.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.file.domain.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件Mapper
 */
@Mapper
public interface FileMapper extends BaseMapper<SysFile> {
}
