package com.platform.project.controller;

import com.platform.common.core.result.Result;
import com.platform.project.domain.dto.AiTaskDecomposeDTO;
import com.platform.project.domain.vo.AiTaskDecomposeVO;
import com.platform.project.service.AiTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI任务控制器
 */
@Tag(name = "AI任务管理", description = "AI任务拆解相关接口")
@RestController
@RequestMapping("/ai/tasks")
@RequiredArgsConstructor
public class AiTaskController {

    private final AiTaskService aiTaskService;

    @Operation(summary = "AI任务拆解", description = "将需求拆解为多个子任务（仅返回拆解结果，不创建任务）")
    @PostMapping("/decompose")
    public Result<List<AiTaskDecomposeVO>> decompose(@Valid @RequestBody AiTaskDecomposeDTO dto) {
        List<AiTaskDecomposeVO> result = aiTaskService.decompose(dto);
        return Result.success(result);
    }

    @Operation(summary = "AI任务拆解并创建", description = "将需求拆解为多个子任务并自动创建到数据库")
    @PostMapping("/decompose-and-create")
    public Result<List<Long>> decomposeAndCreate(@Valid @RequestBody AiTaskDecomposeDTO dto) {
        List<Long> taskIds = aiTaskService.decomposeAndCreate(dto);
        return Result.success(taskIds);
    }
}
