package com.platform.report.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.report.domain.dto.DataSetCreateDTO;
import com.platform.report.domain.dto.DataSetQueryDTO;
import com.platform.report.domain.dto.DataSetUpdateDTO;
import com.platform.report.domain.vo.DataSetVO;
import com.platform.report.service.DataSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据集管理控制器
 */
@Tag(name = "数据集管理", description = "数据集配置相关接口")
@RestController
@RequestMapping("/datasets")
@RequiredArgsConstructor
public class DataSetController {

    private final DataSetService dataSetService;

    @Operation(summary = "分页查询数据集")
    @GetMapping
    public Result<PageResult<DataSetVO>> page(DataSetQueryDTO queryDTO) {
        PageResult<DataSetVO> result = dataSetService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取数据集详情")
    @GetMapping("/{id}")
    public Result<DataSetVO> getById(@PathVariable Long id) {
        DataSetVO vo = dataSetService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("report:dataset:add")
    @Operation(summary = "创建数据集")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DataSetCreateDTO createDTO) {
        Long id = dataSetService.create(createDTO);
        return Result.success(id);
    }

    @RequirePermission("report:dataset:edit")
    @Operation(summary = "更新数据集")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DataSetUpdateDTO updateDTO) {
        dataSetService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("report:dataset:delete")
    @Operation(summary = "删除数据集")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dataSetService.delete(id);
        return Result.success();
    }

    @Operation(summary = "预览数据集数据")
    @PostMapping("/{id}/preview")
    public Result<List<Map<String, Object>>> preview(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        List<Map<String, Object>> data = dataSetService.preview(id, params);
        return Result.success(data);
    }

    @Operation(summary = "获取数据集字段")
    @GetMapping("/{id}/fields")
    public Result<List<Map<String, Object>>> getFields(@PathVariable Long id) {
        List<Map<String, Object>> fields = dataSetService.getFields(id);
        return Result.success(fields);
    }
}
