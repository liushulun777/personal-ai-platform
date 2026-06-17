package com.platform.resource.convert;

import com.platform.resource.domain.entity.ServiceResourceEntity;
import com.platform.resource.domain.vo.ServiceResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResourceConvert {

    ResourceConvert INSTANCE = Mappers.getMapper(ResourceConvert.class);

    @Mapping(source = "cpuUsage", target = "avgCpuUsage")
    @Mapping(source = "memoryUsage", target = "avgMemoryUsage")
    @Mapping(target = "sampleCount", ignore = true)
    @Mapping(source = "recordTime", target = "lastUpdateTime")
    ServiceResourceVO entityToVo(ServiceResourceEntity entity);

    List<ServiceResourceVO> entityListToVoList(List<ServiceResourceEntity> entities);
}