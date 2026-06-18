package com.platform.ai.feign;

import com.platform.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * file-service Feign 客户端
 */
@FeignClient(name = "file-service", contextId = "fileServiceClient")
public interface FileServiceClient {

    /**
     * 上传文件
     */
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<Map<String, Object>> upload(@RequestPart("file") MultipartFile file);
}
