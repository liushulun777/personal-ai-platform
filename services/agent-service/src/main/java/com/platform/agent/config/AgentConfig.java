package com.platform.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agent")
public class AgentConfig {

    private WorkspaceConfig workspace = new WorkspaceConfig();
    private ExecutionConfig execution = new ExecutionConfig();

    @Data
    public static class WorkspaceConfig {
        /**
         * 工作区基础路径
         */
        private String basePath = "workspace";

        /**
         * Git 仓库地址
         */
        private String gitUrl;
    }

    @Data
    public static class ExecutionConfig {
        /**
         * 最大重试次数
         */
        private int maxRetries = 3;

        /**
         * 执行超时时间（毫秒）
         */
        private long timeout = 300000;
    }
}
