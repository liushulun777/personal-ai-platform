package com.platform.report.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.report.domain.dto.DashboardCreateDTO;
import com.platform.report.domain.dto.DashboardQueryDTO;
import com.platform.report.domain.dto.DashboardUpdateDTO;
import com.platform.report.domain.vo.DashboardDetailVO;
import com.platform.report.domain.vo.DashboardVO;
import com.platform.report.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 大屏管理控制器
 */
@Tag(name = "大屏管理", description = "数据大屏相关接口")
@RestController
@RequestMapping("/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "分页查询大屏")
    @GetMapping
    public Result<PageResult<DashboardVO>> page(DashboardQueryDTO queryDTO) {
        PageResult<DashboardVO> result = dashboardService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取大屏详情")
    @GetMapping("/{id}")
    public Result<DashboardDetailVO> getById(@PathVariable Long id) {
        DashboardDetailVO vo = dashboardService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("report:dashboard:add")
    @Operation(summary = "创建大屏")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DashboardCreateDTO createDTO) {
        Long id = dashboardService.create(createDTO);
        return Result.success(id);
    }

    @RequirePermission("report:dashboard:edit")
    @Operation(summary = "更新大屏")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DashboardUpdateDTO updateDTO) {
        dashboardService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("report:dashboard:delete")
    @Operation(summary = "删除大屏")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dashboardService.delete(id);
        return Result.success();
    }

    @RequirePermission("report:dashboard:share")
    @Operation(summary = "分享大屏")
    @PostMapping("/{id}/share")
    public Result<String> share(@PathVariable Long id) {
        String shareUrl = dashboardService.share(id);
        return Result.success(shareUrl);
    }

    @Operation(summary = "获取大屏数据")
    @GetMapping("/{id}/data")
    public Result<Map<String, Object>> getData(@PathVariable Long id) {
        Map<String, Object> data = dashboardService.getData(id);
        return Result.success(data);
    }
}
