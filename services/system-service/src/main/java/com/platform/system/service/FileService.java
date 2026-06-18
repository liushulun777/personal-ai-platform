package com.platform.system.service;

import com.platform.system.domain.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param module 模块名（用于分类存储）
     * @return 文件信息
     */
    FileVO upload(MultipartFile file, String module);

    /**
     * 批量上传文件
     *
     * @param files 文件列表
     * @param module 模块名
     * @return 文件信息列表
     */
    List<FileVO> uploadBatch(List<MultipartFile> files, String module);

    /**
     * 删除文件
     *
     * @param id 文件ID
     */
    void delete(Long id);

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件信息
     */
    FileVO getById(Long id);
}
