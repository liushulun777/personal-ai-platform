package com.platform.file.controller;

import com.platform.common.core.result.Result;
import com.platform.file.domain.vo.FileVO;
import com.platform.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理控制器
 */
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "module", defaultValue = "common") String module) {
        FileVO fileVO = fileService.upload(file, module);
        return Result.success(fileVO);
    }

    @Operation(summary = "批量上传文件")
    @PostMapping("/upload/batch")
    public Result<List<FileVO>> uploadBatch(@RequestParam("files") List<MultipartFile> files,
                                            @RequestParam(value = "module", defaultValue = "common") String module) {
        List<FileVO> fileVOs = fileService.uploadBatch(files, module);
        return Result.success(fileVOs);
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{id}")
    public Result<FileVO> getById(@PathVariable Long id) {
        FileVO fileVO = fileService.getById(id);
        return Result.success(fileVO);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }
}
