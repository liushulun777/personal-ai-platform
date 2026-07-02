package com.platform.report.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

/**
 * 数据集执行器
 */
@Slf4j
public class DataSetExecutor {

    /**
     * 执行SQL查询
     */
    public static List<Map<String, Object>> executeSql(String type, Object config, String sql, Map<String, Object> params) {
        JSONObject configJson = JSONUtil.parseObj(config);

        String url;
        String username;
        String password;
        String driverClassName;

        switch (type.toUpperCase()) {
            case "MYSQL":
                url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8",
                        configJson.getStr("host", "localhost"),
                        configJson.getInt("port", 3306),
                        configJson.getStr("database", ""));
                username = configJson.getStr("username", "");
                password = configJson.getStr("password", "");
                driverClassName = "com.mysql.cj.jdbc.Driver";
                break;
            case "POSTGRESQL":
                url = String.format("jdbc:postgresql://%s:%d/%s",
                        configJson.getStr("host", "localhost"),
                        configJson.getInt("port", 5432),
                        configJson.getStr("database", ""));
                username = configJson.getStr("username", "");
                password = configJson.getStr("password", "");
                driverClassName = "org.postgresql.Driver";
                break;
            default:
                throw new RuntimeException("不支持的数据库类型: " + type);
        }

        return executeJdbcQuery(url, username, password, driverClassName, sql, params);
    }

    /**
     * 执行JDBC查询
     */
    private static List<Map<String, Object>> executeJdbcQuery(String url, String username, String password,
                                                                String driverClassName, String sql, Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            Class.forName(driverClassName);
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                // 替换SQL参数
                String processedSql = replaceSqlParams(sql, params);

                try (PreparedStatement stmt = conn.prepareStatement(processedSql);
                     ResultSet rs = stmt.executeQuery()) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        result.add(row);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("数据库驱动未找到: {}", driverClassName, e);
            throw new RuntimeException("数据库驱动未找到: " + driverClassName);
        } catch (SQLException e) {
            log.error("SQL执行失败: {}", sql, e);
            throw new RuntimeException("SQL执行失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 替换SQL参数
     */
    private static String replaceSqlParams(String sql, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return sql;
        }

        String result = sql;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String placeholder = "${" + key + "}";

            if (result.contains(placeholder)) {
                String valueStr = value == null ? "NULL" : "'" + value.toString().replace("'", "''") + "'";
                result = result.replace(placeholder, valueStr);
            }
        }

        return result;
    }

    /**
     * 获取SQL查询的字段信息
     */
    public static List<Map<String, Object>> getSqlFields(String type, Object config, String sql) {
        JSONObject configJson = JSONUtil.parseObj(config);

        String url;
        String username;
        String password;
        String driverClassName;

        switch (type.toUpperCase()) {
            case "MYSQL":
                url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8",
                        configJson.getStr("host", "localhost"),
                        configJson.getInt("port", 3306),
                        configJson.getStr("database", ""));
                username = configJson.getStr("username", "");
                password = configJson.getStr("password", "");
                driverClassName = "com.mysql.cj.jdbc.Driver";
                break;
            case "POSTGRESQL":
                url = String.format("jdbc:postgresql://%s:%d/%s",
                        configJson.getStr("host", "localhost"),
                        configJson.getInt("port", 5432),
                        configJson.getStr("database", ""));
                username = configJson.getStr("username", "");
                password = configJson.getStr("password", "");
                driverClassName = "org.postgresql.Driver";
                break;
            default:
                throw new RuntimeException("不支持的数据库类型: " + type);
        }

        return getJdbcFields(url, username, password, driverClassName, sql);
    }

    /**
     * 获取JDBC查询的字段信息
     */
    private static List<Map<String, Object>> getJdbcFields(String url, String username, String password,
                                                             String driverClassName, String sql) {
        List<Map<String, Object>> fields = new ArrayList<>();

        try {
            Class.forName(driverClassName);
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                // 添加LIMIT 1避免查询大量数据
                String limitedSql = sql.replaceAll(";\\s*$", "") + " LIMIT 1";

                try (PreparedStatement stmt = conn.prepareStatement(limitedSql);
                     ResultSet rs = stmt.executeQuery()) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        Map<String, Object> field = new LinkedHashMap<>();
                        field.put("name", metaData.getColumnLabel(i));
                        field.put("label", metaData.getColumnLabel(i));
                        field.put("type", getFieldType(metaData.getColumnType(i)));
                        fields.add(field);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("数据库驱动未找到: {}", driverClassName, e);
            throw new RuntimeException("数据库驱动未找到: " + driverClassName);
        } catch (SQLException e) {
            log.error("获取字段信息失败: {}", sql, e);
            throw new RuntimeException("获取字段信息失败: " + e.getMessage());
        }

        return fields;
    }

    /**
     * 获取字段类型
     */
    private static String getFieldType(int sqlType) {
        switch (sqlType) {
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                return "NUMBER";
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
                return "NUMBER";
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIME:
                return "DATE";
            case Types.BOOLEAN:
                return "BOOLEAN";
            default:
                return "STRING";
        }
    }

    /**
     * 执行API查询
     */
    public static List<Map<String, Object>> executeApi(Object config, Map<String, Object> params) {
        JSONObject configJson = JSONUtil.parseObj(config);

        String url = configJson.getStr("url", "");
        String method = configJson.getStr("method", "GET");
        JSONObject headers = configJson.getJSONObject("headers");
        String responseMapping = configJson.getStr("responseMapping", "data");

        // 替换URL参数
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                url = url.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

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

            HttpResponse response = request.timeout(10000).execute();

            if (!response.isOk()) {
                throw new RuntimeException("API请求失败: HTTP " + response.getStatus());
            }

            // 解析响应
            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);

            // 根据映射获取数据
            Object data = json.getByPath(responseMapping);
            if (data instanceof JSONArray jsonArray) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    if (item != null) {
                        result.add(item);
                    }
                }
                return result;
            } else if (data instanceof JSONObject jsonObject) {
                return Collections.singletonList(jsonObject);
            } else {
                return Collections.singletonList(MapUtil.of("value", data));
            }
        } catch (Exception e) {
            log.error("API执行失败: {}", url, e);
            throw new RuntimeException("API执行失败: " + e.getMessage());
        }
    }
}
