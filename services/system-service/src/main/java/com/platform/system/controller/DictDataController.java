package com.platform.system.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.common.security.annotation.RequirePermission;
import com.platform.system.domain.dto.DictDataCreateDTO;
import com.platform.system.domain.dto.DictDataQueryDTO;
import com.platform.system.domain.dto.DictDataUpdateDTO;
import com.platform.system.domain.vo.DictDataVO;
import com.platform.system.service.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据管理控制器
 */
@Tag(name = "字典数据管理", description = "字典数据CRUD相关接口")
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataService dictDataService;

    @Operation(summary = "分页查询字典数据")
    @GetMapping
    public Result<PageResult<DictDataVO>> page(DictDataQueryDTO queryDTO) {
        PageResult<DictDataVO> result = dictDataService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "根据字典类型获取字典数据列表")
    @GetMapping("/type/{dictType}")
    public Result<List<DictDataVO>> listByDictType(@PathVariable String dictType) {
        List<DictDataVO> list = dictDataService.listByDictType(dictType);
        return Result.success(list);
    }

    @Operation(summary = "获取字典数据详情")
    @GetMapping("/{id}")
    public Result<DictDataVO> getById(@PathVariable Long id) {
        DictDataVO vo = dictDataService.getById(id);
        return Result.success(vo);
    }

    @RequirePermission("system:dict:add")
    @Operation(summary = "创建字典数据")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DictDataCreateDTO createDTO) {
        Long dictDataId = dictDataService.create(createDTO);
        return Result.success(dictDataId);
    }

    @RequirePermission("system:dict:edit")
    @Operation(summary = "更新字典数据")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DictDataUpdateDTO updateDTO) {
        dictDataService.update(id, updateDTO);
        return Result.success();
    }

    @RequirePermission("system:dict:delete")
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictDataService.delete(id);
        return Result.success();
    }
}
