package com.platform.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.ai.domain.dto.PromptCreateDTO;
import com.platform.ai.domain.entity.Prompt;
import com.platform.ai.domain.vo.PromptVO;
import com.platform.ai.mapper.PromptMapper;
import com.platform.ai.service.PromptService;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Prompt模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final PromptMapper promptMapper;

    @Override
    public PageResult<PromptVO> listPrompts(Integer current, Integer size) {
        Page<Prompt> page = new Page<>(current, size);
        LambdaQueryWrapper<Prompt> wrapper = new LambdaQueryWrapper<Prompt>()
                .eq(Prompt::getStatus, 1)
                .orderByDesc(Prompt::getCreateTime);

        Page<Prompt> result = promptMapper.selectPage(page, wrapper);

        List<PromptVO> records = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, result.getTotal(), current.longValue(), size.longValue());
    }

    @Override
    public List<PromptVO> listByCategory(String category) {
        LambdaQueryWrapper<Prompt> wrapper = new LambdaQueryWrapper<Prompt>()
                .eq(Prompt::getStatus, 1)
                .eq(Prompt::getCategory, category)
                .orderByDesc(Prompt::getCreateTime);

        List<Prompt> prompts = promptMapper.selectList(wrapper);
        return prompts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getCategories() {
        List<Prompt> prompts = promptMapper.selectList(
                new LambdaQueryWrapper<Prompt>().eq(Prompt::getStatus, 1)
        );
        return prompts.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() != null ? p.getCategory() : "other",
                        Collectors.counting()
                ));
    }

    @Override
    public PromptVO createPrompt(PromptCreateDTO dto) {
        try {
            Prompt prompt = new Prompt();
            BeanUtils.copyProperties(dto, prompt);
            prompt.setStatus(1);
            promptMapper.insert(prompt);
            return convertToVO(prompt);
        } catch (Exception e) {
            log.error("创建Prompt模板失败", e);
            throw new BusinessException(ResultCode.FAIL, "创建失败: " + e.getMessage());
        }
    }

    @Override
    public void deletePrompt(Long id) {
        Prompt prompt = promptMapper.selectById(id);
        if (prompt == null) {
            throw new BusinessException(ResultCode.FAIL, "模板不存在");
        }
        promptMapper.deleteById(id);
    }

    private PromptVO convertToVO(Prompt prompt) {
        PromptVO vo = new PromptVO();
        BeanUtils.copyProperties(prompt, vo);
        return vo;
    }
}
