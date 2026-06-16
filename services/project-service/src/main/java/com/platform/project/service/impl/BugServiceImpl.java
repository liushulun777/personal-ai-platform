package com.platform.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.common.security.util.SecurityUtils;
import com.platform.project.convert.BugConvert;
import com.platform.project.domain.dto.BugCreateDTO;
import com.platform.project.domain.dto.BugQueryDTO;
import com.platform.project.domain.dto.BugUpdateDTO;
import com.platform.project.domain.entity.Bug;
import com.platform.project.domain.vo.BugVO;
import com.platform.project.mapper.BugMapper;
import com.platform.project.service.BugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Bug服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

    private final BugMapper bugMapper;
    private final BugConvert bugConvert;

    @Override
    public PageResult<BugVO> page(BugQueryDTO queryDTO) {
        LambdaQueryWrapper<Bug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getProjectId() != null, Bug::getProjectId, queryDTO.getProjectId());
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Bug::getTitle, queryDTO.getTitle());
        wrapper.eq(queryDTO.getSeverity() != null, Bug::getSeverity, queryDTO.getSeverity());
        wrapper.eq(queryDTO.getStatus() != null, Bug::getStatus, queryDTO.getStatus());
        wrapper.eq(queryDTO.getAssigneeId() != null, Bug::getAssigneeId, queryDTO.getAssigneeId());
        wrapper.orderByDesc(Bug::getCreateTime);

        Page<Bug> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Bug> result = bugMapper.selectPage(page, wrapper);

        List<BugVO> records = bugConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public BugVO getById(Long id) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Bug不存在");
        }
        return bugConvert.entityToVO(bug);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BugCreateDTO dto) {
        Bug bug = bugConvert.createDTOToEntity(dto);
        if (bug.getStatus() == null) {
            bug.setStatus(0);
        }
        if (bug.getSeverity() == null) {
            bug.setSeverity(1);
        }
        bug.setReporterId(SecurityUtils.getCurrentUserIdOrNull());
        bugMapper.insert(bug);
        return bug.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BugUpdateDTO dto) {
        Bug bug = bugMapper.selectById(dto.getId());
        if (bug == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Bug不存在");
        }
        bugConvert.updateEntityFromDTO(dto, bug);
        bugMapper.updateById(bug);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Bug不存在");
        }
        bugMapper.deleteById(id);
    }
}
