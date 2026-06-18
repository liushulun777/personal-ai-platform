package com.platform.system.service.impl;

import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.ResultCode;
import com.platform.system.domain.entity.SysFile;
import com.platform.system.domain.vo.FileVO;
import com.platform.system.mapper.FileMapper;
import com.platform.system.service.FileService;
import com.platform.system.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件服务实现
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private final MinioService minioService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO upload(MultipartFile file, String module) {
        // 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "文件不能为空");
        }

        // 获取原始文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unknown";
        }

        // 上传到MinIO
        String filePath = minioService.uploadFile(file, module);
        String fileUrl = minioService.getFileUrl(filePath);

        // 保存文件信息到数据库
        SysFile sysFile = new SysFile();
        sysFile.setFileName(filePath);
        sysFile.setOriginalName(originalName);
        sysFile.setFilePath(filePath);
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(getFileExtension(originalName));
        sysFile.setMimeType(file.getContentType());
        sysFile.setStorageType("MINIO");
        fileMapper.insert(sysFile);

        // 返回文件信息
        return convertToVO(sysFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileVO> uploadBatch(List<MultipartFile> files, String module) {
        List<FileVO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(upload(file, module));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文件不存在");
        }

        // 从MinIO删除
        minioService.deleteFile(sysFile.getFilePath());

        // 从数据库删除
        fileMapper.deleteById(id);
    }

    @Override
    public FileVO getById(Long id) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文件不存在");
        }

        // 刷新URL
        String fileUrl = minioService.getFileUrl(sysFile.getFilePath());
        sysFile.setFileUrl(fileUrl);

        return convertToVO(sysFile);
    }

    /**
     * 转换为VO
     */
    private FileVO convertToVO(SysFile sysFile) {
        FileVO vo = new FileVO();
        vo.setId(sysFile.getId());
        vo.setFileName(sysFile.getFileName());
        vo.setOriginalName(sysFile.getOriginalName());
        vo.setFileUrl(sysFile.getFileUrl());
        vo.setFileSize(sysFile.getFileSize());
        vo.setFileType(sysFile.getFileType());
        vo.setMimeType(sysFile.getMimeType());
        vo.setCreateTime(sysFile.getCreateTime());
        return vo;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "";
    }
}
