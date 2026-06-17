package com.platform.project.service;

import com.platform.project.domain.dto.AiTaskDecomposeDTO;
import com.platform.project.domain.vo.AiTaskDecomposeVO;

import java.util.List;

/**
 * AI任务服务接口
 */
public interface AiTaskService {

    /**
     * AI任务拆解
     * 将需求拆解为多个子任务
     */
    List<AiTaskDecomposeVO> decompose(AiTaskDecomposeDTO dto);

    /**
     * AI任务拆解并自动创建任务
     * 将需求拆解为多个子任务并自动写入数据库
     */
    List<Long> decomposeAndCreate(AiTaskDecomposeDTO dto);
}
