package com.platform.common.core.handler;

import com.platform.common.core.annotation.OperationLog;
import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志处理器接口
 * 各服务实现此接口来自定义日志处理逻辑
 */
public interface OperationLogHandler {

    /**
     * 保存操作日志
     *
     * @param logInfo 日志信息
     */
    void saveLog(OperationLogInfo logInfo);

    /**
     * 日志信息
     */
    @Setter
    @Getter
    class OperationLogInfo {
        private String module;
        private String operation;
        private String type;
        private String method;
        private String requestUrl;
        private String requestMethod;
        private String ip;
        private String userAgent;
        private String requestParams;
        private String responseData;
        private String errorMsg;
        private Integer status;
        private Long duration;
        private Long userId;

    }
}
