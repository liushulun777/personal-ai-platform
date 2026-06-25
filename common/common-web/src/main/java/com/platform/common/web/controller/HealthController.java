package com.platform.common.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
public class HealthController {

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());

        // 运行时信息
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        result.put("uptime", runtime.getUptime());

        // 内存信息
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memoryInfo = new LinkedHashMap<>();
        memoryInfo.put("heapUsed", memory.getHeapMemoryUsage().getUsed() / 1024 / 1024 + " MB");
        memoryInfo.put("heapMax", memory.getHeapMemoryUsage().getMax() / 1024 / 1024 + " MB");
        memoryInfo.put("nonHeapUsed", memory.getNonHeapMemoryUsage().getUsed() / 1024 / 1024 + " MB");
        result.put("memory", memoryInfo);

        // JVM 信息
        Map<String, Object> jvmInfo = new LinkedHashMap<>();
        jvmInfo.put("javaVersion", System.getProperty("java.version"));
        jvmInfo.put("jvmName", System.getProperty("java.vm.name"));
        jvmInfo.put("processors", Runtime.getRuntime().availableProcessors());
        result.put("jvm", jvmInfo);

        return result;
    }

    /**
     * 就绪检查（用于 K8s readinessProbe）
     */
    @GetMapping("/health/ready")
    public Map<String, String> ready() {
        return Map.of("status", "READY");
    }

    /**
     * 存活检查（用于 K8s livenessProbe）
     */
    @GetMapping("/health/live")
    public Map<String, String> live() {
        return Map.of("status", "ALIVE");
    }
}
