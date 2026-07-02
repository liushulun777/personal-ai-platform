package com.platform.report.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.report.domain.dto.ScheduleCreateDTO;
import com.platform.report.domain.dto.ScheduleQueryDTO;
import com.platform.report.domain.dto.ScheduleUpdateDTO;
import com.platform.report.domain.vo.ScheduleDetailVO;
import com.platform.report.domain.vo.ScheduleVO;
import com.platform.report.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 调度管理控制器
 */
@Tag(name = "调度管理", description = "定时调度任务相关接口")
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "分页查询调度任务")
    @GetMapping
    public Result<PageResult<ScheduleVO>> page(ScheduleQueryDTO queryDTO) {
        PageResult<ScheduleVO> result = scheduleService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取调度任务详情")
    @GetMapping("/{id}")
    public Result<ScheduleDetailVO> getById(@PathVariable Long id) {
        ScheduleDetailVO vo = scheduleService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("report:schedule:add")
    @Operation(summary = "创建调度任务")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ScheduleCreateDTO createDTO) {
        Long id = scheduleService.create(createDTO);
        return Result.success(id);
    }

    @RequirePermission("report:schedule:edit")
    @Operation(summary = "更新调度任务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ScheduleUpdateDTO updateDTO) {
        scheduleService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("report:schedule:delete")
    @Operation(summary = "删除调度任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return Result.success();
    }

    @RequirePermission("report:schedule:manage")
    @Operation(summary = "启动调度任务")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        scheduleService.start(id);
        return Result.success();
    }

    @RequirePermission("report:schedule:manage")
    @Operation(summary = "停止调度任务")
    @PostMapping("/{id}/stop")
    public Result<Void> stop(@PathVariable Long id) {
        scheduleService.stop(id);
        return Result.success();
    }

    @RequirePermission("report:schedule:manage")
    @Operation(summary = "暂停调度任务")
    @PostMapping("/{id}/pause")
    public Result<Void> pause(@PathVariable Long id) {
        scheduleService.pause(id);
        return Result.success();
    }
}
