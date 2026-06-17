package com.platform.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.resource.domain.entity.ServiceResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ServiceResourceMapper extends BaseMapper<ServiceResourceEntity> {

    List<ServiceResourceEntity> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<ServiceResourceEntity> selectLatestByService();
}