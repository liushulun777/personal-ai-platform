package com.platform.report.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.report.domain.dto.ReportCreateDTO;
import com.platform.report.domain.dto.ReportQueryDTO;
import com.platform.report.domain.dto.ReportUpdateDTO;
import com.platform.report.domain.vo.ReportDetailVO;
import com.platform.report.domain.vo.ReportVO;
import com.platform.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 报表管理控制器
 */
@Tag(name = "报表管理", description = "报表定义和展示相关接口")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "分页查询报表")
    @GetMapping
    public Result<PageResult<ReportVO>> page(ReportQueryDTO queryDTO) {
        PageResult<ReportVO> result = reportService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取报表详情")
    @GetMapping("/{id}")
    public Result<ReportDetailVO> getById(@PathVariable Long id) {
        ReportDetailVO vo = reportService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("report:report:add")
    @Operation(summary = "创建报表")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ReportCreateDTO createDTO) {
        Long id = reportService.create(createDTO);
        return Result.success(id);
    }

    @RequirePermission("report:report:edit")
    @Operation(summary = "更新报表")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ReportUpdateDTO updateDTO) {
        reportService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("report:report:delete")
    @Operation(summary = "删除报表")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return Result.success();
    }

    @RequirePermission("report:report:publish")
    @Operation(summary = "发布报表")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        reportService.publish(id);
        return Result.success();
    }

    @RequirePermission("report:report:publish")
    @Operation(summary = "归档报表")
    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        reportService.archive(id);
        return Result.success();
    }

    @Operation(summary = "获取报表数据")
    @PostMapping("/{id}/data")
    public Result<Map<String, Object>> getData(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> data = reportService.getData(id, params);
        return Result.success(data);
    }

    @RequirePermission("report:report:export")
    @Operation(summary = "导出报表")
    @PostMapping("/{id}/export")
    public Result<String> export(@PathVariable Long id, @RequestParam String format, @RequestBody Map<String, Object> params) {
        String filePath = reportService.export(id, format, params);
        return Result.success(filePath);
    }
}
