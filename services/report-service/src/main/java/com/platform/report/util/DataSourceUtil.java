package com.platform.report.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * 数据源连接工具类
 */
@Slf4j
public class DataSourceUtil {

    /**
     * 测试MySQL连接
     */
    public static boolean testMySQLConnection(String host, int port, String database, String username, String password) {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8", host, port, database);
        return testJdbcConnection(url, username, password, "com.mysql.cj.jdbc.Driver");
    }

    /**
     * 测试PostgreSQL连接
     */
    public static boolean testPostgreSQLConnection(String host, int port, String database, String username, String password) {
        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        return testJdbcConnection(url, username, password, "org.postgresql.Driver");
    }

    /**
     * 测试JDBC连接
     */
    private static boolean testJdbcConnection(String url, String username, String password, String driverClassName) {
        try {
            Class.forName(driverClassName);
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                return conn != null && !conn.isClosed();
            }
        } catch (ClassNotFoundException e) {
            log.error("数据库驱动未找到: {}", driverClassName, e);
            return false;
        } catch (SQLException e) {
            log.error("数据库连接失败: {}", url, e);
            return false;
        }
    }

    /**
     * 测试API连接
     */
    public static boolean testApiConnection(String url, String method, JSONObject headers) {
        try {
            HttpRequest request;
            if ("POST".equalsIgnoreCase(method)) {
                request = HttpRequest.post(url);
            } else {
                request = HttpRequest.get(url);
            }

            if (headers != null) {
                headers.forEach((k, v) -> request.header(k, String.valueOf(v)));
            }

            HttpResponse response = request.timeout(5000).execute();
            return response.isOk();
        } catch (Exception e) {
            log.error("API连接失败: {}", url, e);
            return false;
        }
    }

    /**
     * 根据数据源配置测试连接
     */
    public static boolean testConnection(String type, Object config) {
        if (config == null) {
            return false;
        }

        JSONObject configJson = JSONUtil.parseObj(config);

        switch (type.toUpperCase()) {
            case "MYSQL":
                return testMySQLConnection(
                        configJson.getStr("host", "localhost"),
                        configJson.getInt("port", 3306),
                        configJson.getStr("database", ""),
                        configJson.getStr("username", ""),
                        configJson.getStr("password", "")
                );
            case "POSTGRESQL":
                return testPostgreSQLConnection(
                        configJson.getStr("host", "localhost"),
                        configJson.getInt("port", 5432),
                        configJson.getStr("database", ""),
                        configJson.getStr("username", ""),
                        configJson.getStr("password", "")
                );
            case "API":
                return testApiConnection(
                        configJson.getStr("url", ""),
                        configJson.getStr("method", "GET"),
                        configJson.getJSONObject("headers")
                );
            case "EXCEL":
            case "CSV":
                // 文件类型暂时返回true，后续可以检查文件是否存在
                return true;
            default:
                log.warn("不支持的数据源类型: {}", type);
                return false;
        }
    }
}
