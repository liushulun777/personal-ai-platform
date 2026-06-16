package com.platform.system.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.system.domain.dto.DictTypeCreateDTO;
import com.platform.system.domain.dto.DictTypeQueryDTO;
import com.platform.system.domain.dto.DictTypeUpdateDTO;
import com.platform.system.domain.vo.DictTypeVO;
import com.platform.system.service.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型管理控制器
 */
@Tag(name = "字典类型管理", description = "字典类型CRUD相关接口")
@RestController
@RequestMapping("/dict/types")
@RequiredArgsConstructor
public class DictTypeController {

    private final DictTypeService dictTypeService;

    @Operation(summary = "分页查询字典类型")
    @GetMapping
    public Result<PageResult<DictTypeVO>> page(DictTypeQueryDTO queryDTO) {
        PageResult<DictTypeVO> result = dictTypeService.page(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取所有字典类型列表")
    @GetMapping("/all")
    public Result<List<DictTypeVO>> listAll() {
        List<DictTypeVO> list = dictTypeService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/{id}")
    public Result<DictTypeVO> getById(@PathVariable Long id) {
        DictTypeVO vo = dictTypeService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建字典类型")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DictTypeCreateDTO createDTO) {
        Long dictTypeId = dictTypeService.create(createDTO);
        return Result.success(dictTypeId);
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DictTypeUpdateDTO updateDTO) {
        dictTypeService.update(id, updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return Result.success();
    }
}
