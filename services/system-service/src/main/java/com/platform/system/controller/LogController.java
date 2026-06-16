package com.platform.system.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.system.domain.dto.LogQueryDTO;
import com.platform.system.domain.vo.LogVO;
import com.platform.system.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志管理", description = "操作日志查看相关接口")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public Result<PageResult<LogVO>> page(LogQueryDTO queryDTO) {
        PageResult<LogVO> result = logService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取操作日志详情")
    @GetMapping("/{id}")
    public Result<LogVO> getById(@PathVariable Long id) {
        LogVO vo = logService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        logService.delete(id);
        return Result.success();
    }
}
