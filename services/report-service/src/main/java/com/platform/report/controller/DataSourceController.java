package com.platform.report.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.report.domain.dto.DataSourceCreateDTO;
import com.platform.report.domain.dto.DataSourceQueryDTO;
import com.platform.report.domain.dto.DataSourceUpdateDTO;
import com.platform.report.domain.vo.DataSourceVO;
import com.platform.report.service.DataSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 数据源管理控制器
 */
@Tag(name = "数据源管理", description = "数据源连接配置相关接口")
@RestController
@RequestMapping("/datasources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @Operation(summary = "分页查询数据源")
    @GetMapping
    public Result<PageResult<DataSourceVO>> page(DataSourceQueryDTO queryDTO) {
        PageResult<DataSourceVO> result = dataSourceService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取数据源详情")
    @GetMapping("/{id}")
    public Result<DataSourceVO> getById(@PathVariable Long id) {
        DataSourceVO vo = dataSourceService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("report:datasource:add")
    @Operation(summary = "创建数据源")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DataSourceCreateDTO createDTO) {
        Long id = dataSourceService.create(createDTO);
        return Result.success(id);
    }

    @RequirePermission("report:datasource:edit")
    @Operation(summary = "更新数据源")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DataSourceUpdateDTO updateDTO) {
        dataSourceService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("report:datasource:delete")
    @Operation(summary = "删除数据源")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dataSourceService.delete(id);
        return Result.success();
    }

    @Operation(summary = "测试数据源连接")
    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        boolean success = dataSourceService.testConnection(id);
        return Result.success(success);
    }
}
