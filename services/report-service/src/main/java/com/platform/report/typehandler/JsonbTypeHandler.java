package com.platform.report.typehandler;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL JSONB 类型处理器
 * 在 Object 和 PostgreSQL jsonb 类型之间转换
 */
@Slf4j
@MappedTypes(Object.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        pgObject.setValue(JSONUtil.toJsonStr(parameter));
        ps.setObject(i, pgObject);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseJson(value);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseJson(value);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseJson(value);
    }

    /**
     * 解析JSON字符串，失败返回null
     */
    private Object parseJson(String value) {
        if (value == null || value.isEmpty() || "null".equals(value)) {
            return null;
        }
        try {
            // 尝试解析为JSONObject
            if (value.trim().startsWith("{")) {
                return JSONUtil.parseObj(value);
            }
            // 尝试解析为JSONArray
            if (value.trim().startsWith("[")) {
                return JSONUtil.parseArray(value);
            }
            // 其他情况返回原始值
            return value;
        } catch (Exception e) {
            log.warn("JSON解析失败，返回原始值: {}", value, e);
            return value;
        }
    }
}
