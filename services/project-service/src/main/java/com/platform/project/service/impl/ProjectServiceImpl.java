package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.convert.ProjectConvert;
import com.platform.project.domain.dto.ProjectCreateDTO;
import com.platform.project.domain.dto.ProjectQueryDTO;
import com.platform.project.domain.dto.ProjectUpdateDTO;
import com.platform.project.domain.entity.Project;
import com.platform.project.domain.vo.ProjectVO;
import com.platform.project.mapper.ProjectMapper;
import com.platform.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 项目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectConvert projectConvert;

    @Override
    public PageResult<ProjectVO> page(ProjectQueryDTO queryDTO) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), Project::getName, queryDTO.getName());
        wrapper.eq(queryDTO.getStatus() != null, Project::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getPriority() != null, Project::getPriority, queryDTO.getPriority());
        wrapper.eq(queryDTO.getOwnerId() != null, Project::getOwnerId, queryDTO.getOwnerId());
        wrapper.orderByDesc(Project::getCreateTime);

        Page<Project> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Project> result = projectMapper.selectPage(page, wrapper);

        List<ProjectVO> records = projectConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        return projectConvert.entityToVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectCreateDTO dto) {
        Project project = projectConvert.createDTOToEntity(dto);
        if (project.getStatus() == null) {
            project.setStatus(0);
        }
        if (project.getPriority() == null) {
            project.setPriority(1);
        }
        projectMapper.insert(project);
        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectUpdateDTO dto) {
        Project project = projectMapper.selectById(dto.getId());
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        projectConvert.updateEntityFromDTO(dto, project);
        projectMapper.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        projectMapper.deleteById(id);
    }
}
