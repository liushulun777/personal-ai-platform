package com.platform.ai.feign;

import com.platform.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

/**
 * FileServiceClient 降级工厂
 */
@Slf4j
@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {

    @Override
    public FileServiceClient create(Throwable cause) {
        return new FileServiceClient() {
            @Override
            public Result<Map<String, Object>> upload(MultipartFile file) {
                log.error("文件上传失败，文件名: {}, 原因: {}", file.getOriginalFilename(), cause.getMessage());
                return Result.fail("文件上传失败，请稍后重试");
            }
        };
    }
}