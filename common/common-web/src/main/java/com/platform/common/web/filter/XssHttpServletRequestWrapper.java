package com.platform.common.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.regex.Pattern;

/**
 * XSS 请求包装器
 * 对请求参数进行 XSS 过滤
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 需要过滤的 XSS 关键字模式
     */
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\\r\\n]*=[\\r\\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
    };

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }

        String[] filteredValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            filteredValues[i] = cleanXss(values[i]);
        }
        return filteredValues;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value != null ? cleanXss(value) : null;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return value != null ? cleanXss(value) : null;
    }

    /**
     * 清理 XSS 内容
     */
    private String cleanXss(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 转义 HTML 特殊字符
        value = value.replaceAll("<", "&lt;")
                     .replaceAll(">", "&gt;")
                     .replaceAll("\"", "&quot;")
                     .replaceAll("'", "&#39;");

        // 移除危险的脚本标签和事件
        for (Pattern pattern : XSS_PATTERNS) {
            value = pattern.matcher(value).replaceAll("");
        }

        return value;
    }
}
