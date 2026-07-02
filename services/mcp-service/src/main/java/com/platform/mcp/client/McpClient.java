package com.platform.mcp.client;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.mcp.domain.entity.McpServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * MCP 协议客户端
 * 支持 stdio、sse、streamable_http 三种传输方式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用 MCP 工具
     */
    @SentinelResource(
        value = "invokeTool",
        blockHandler = "invokeToolBlockHandler",
        fallback = "invokeToolFallback"
    )
    public Map<String, Object> invokeTool(McpServer server, String toolName, Map<String, Object> arguments) {
        String transportType = server.getTransportType();

        return switch (transportType) {
            case "stdio" -> invokeViaStdio(server, toolName, arguments);
            case "sse" -> invokeViaSse(server, toolName, arguments);
            case "streamable_http" -> invokeViaHttp(server, toolName, arguments);
            default -> throw new IllegalArgumentException("不支持的传输类型: " + transportType);
        };
    }

    /**
     * 限流/降级处理方法（BlockException）
     */
    public Map<String, Object> invokeToolBlockHandler(McpServer server, String toolName,
                                                      Map<String, Object> arguments, BlockException ex) {
        log.warn("MCP工具调用被限流或降级，工具: {}, 原因: {}", toolName, ex.getClass().getSimpleName());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "MCP服务暂时不可用");
        fallbackResult.put("message", "请稍后重试");
        return fallbackResult;
    }

    /**
     * 业务异常处理方法（Throwable）
     */
    public Map<String, Object> invokeToolFallback(McpServer server, String toolName,
                                                  Map<String, Object> arguments, Throwable t) {
        log.error("MCP工具调用失败，工具: {}, 原因: {}", toolName, t.getMessage());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "MCP工具调用失败");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    /**
     * 获取 MCP Server 的工具列表
     */
    public List<Map<String, Object>> listTools(McpServer server) {
        String transportType = server.getTransportType();

        return switch (transportType) {
            case "stdio" -> listToolsViaStdio(server);
            case "sse" -> listToolsViaSse(server);
            case "streamable_http" -> listToolsViaHttp(server);
            default -> throw new IllegalArgumentException("不支持的传输类型: " + transportType);
        };
    }

    /**
     * 通过 stdio 调用工具
     */
    private Map<String, Object> invokeViaStdio(McpServer server, String toolName, Map<String, Object> arguments) {
        try {
            // 构建 JSON-RPC 请求
            Map<String, Object> request = buildJsonRpcRequest("tools/call", Map.of(
                    "name", toolName,
                    "arguments", arguments != null ? arguments : Map.of()
            ));

            String requestJson = objectMapper.writeValueAsString(request);

            // 启动进程
            ProcessBuilder pb = new ProcessBuilder();
            String command = server.getCommand();
            if (command != null && !command.isEmpty()) {
                pb.command(command.split("\\s+"));
            }

            // 设置环境变量
            if (server.getEnvVars() != null && !server.getEnvVars().isEmpty()) {
                Map<String, String> envVars = objectMapper.readValue(server.getEnvVars(),
                        new TypeReference<Map<String, String>>() {});
                pb.environment().putAll(envVars);
            }

            Process process = pb.start();

            // 发送请求
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(requestJson.getBytes(StandardCharsets.UTF_8));
            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // 读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // 等待进程完成
            process.waitFor(30, TimeUnit.SECONDS);

            // 解析响应
            return parseJsonRpcResponse(response.toString());
        } catch (Exception e) {
            log.error("stdio 调用失败", e);
            throw new RuntimeException("stdio 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 SSE 调用工具
     */
    private Map<String, Object> invokeViaSse(McpServer server, String toolName, Map<String, Object> arguments) {
        try {
            String endpoint = server.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                throw new IllegalArgumentException("SSE 端点未配置");
            }

            // 构建 JSON-RPC 请求
            Map<String, Object> request = buildJsonRpcRequest("tools/call", Map.of(
                    "name", toolName,
                    "arguments", arguments != null ? arguments : Map.of()
            ));

            // 发送 HTTP POST 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return response.getBody() != null ? (Map<String, Object>) response.getBody() : Map.of();
        } catch (Exception e) {
            log.error("SSE 调用失败", e);
            throw new RuntimeException("SSE 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 HTTP 调用工具
     */
    private Map<String, Object> invokeViaHttp(McpServer server, String toolName, Map<String, Object> arguments) {
        try {
            String endpoint = server.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                throw new IllegalArgumentException("HTTP 端点未配置");
            }

            // 构建 JSON-RPC 请求
            Map<String, Object> request = buildJsonRpcRequest("tools/call", Map.of(
                    "name", toolName,
                    "arguments", arguments != null ? arguments : Map.of()
            ));

            // 发送 HTTP POST 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return response.getBody() != null ? (Map<String, Object>) response.getBody() : Map.of();
        } catch (Exception e) {
            log.error("HTTP 调用失败", e);
            throw new RuntimeException("HTTP 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 stdio 获取工具列表
     */
    private List<Map<String, Object>> listToolsViaStdio(McpServer server) {
        try {
            Map<String, Object> request = buildJsonRpcRequest("tools/list", null);
            String requestJson = objectMapper.writeValueAsString(request);

            ProcessBuilder pb = new ProcessBuilder();
            String command = server.getCommand();
            if (command != null && !command.isEmpty()) {
                pb.command(command.split("\\s+"));
            }

            Process process = pb.start();

            OutputStream outputStream = process.getOutputStream();
            outputStream.write(requestJson.getBytes(StandardCharsets.UTF_8));
            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            process.waitFor(30, TimeUnit.SECONDS);

            Map<String, Object> result = parseJsonRpcResponse(response.toString());
            if (result.containsKey("tools")) {
                return (List<Map<String, Object>>) result.get("tools");
            }
            return List.of();
        } catch (Exception e) {
            log.error("stdio 获取工具列表失败", e);
            throw new RuntimeException("stdio 获取工具列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 SSE 获取工具列表
     */
    private List<Map<String, Object>> listToolsViaSse(McpServer server) {
        return listToolsViaHttp(server); // 实现类似
    }

    /**
     * 通过 HTTP 获取工具列表
     */
    private List<Map<String, Object>> listToolsViaHttp(McpServer server) {
        try {
            String endpoint = server.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                throw new IllegalArgumentException("HTTP 端点未配置");
            }

            Map<String, Object> request = buildJsonRpcRequest("tools/list", null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("tools")) {
                return (List<Map<String, Object>>) body.get("tools");
            }
            return List.of();
        } catch (Exception e) {
            log.error("HTTP 获取工具列表失败", e);
            throw new RuntimeException("HTTP 获取工具列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 JSON-RPC 请求
     */
    private Map<String, Object> buildJsonRpcRequest(String method, Map<String, Object> params) {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", System.currentTimeMillis());
        request.put("method", method);
        if (params != null) {
            request.put("params", params);
        }
        return request;
    }

    /**
     * 解析 JSON-RPC 响应
     */
    private Map<String, Object> parseJsonRpcResponse(String responseJson) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseJson,
                    new TypeReference<Map<String, Object>>() {});

            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                throw new RuntimeException("MCP 调用错误: " + error.get("message"));
            }

            return response.containsKey("result") ? (Map<String, Object>) response.get("result") : response;
        } catch (Exception e) {
            log.error("解析响应失败: {}", responseJson, e);
            throw new RuntimeException("解析响应失败: " + e.getMessage(), e);
        }
    }
}
