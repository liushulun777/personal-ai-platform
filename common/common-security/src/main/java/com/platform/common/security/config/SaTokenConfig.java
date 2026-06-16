package com.platform.common.security.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Sa-Token 配置
 * <p>
 * 使用 SaServletFilter 替代 SaInterceptor，通过 addExclude 可靠地排除公开路径。
 * 公开的博客读取接口仅放行 GET 请求，POST/PUT/DELETE 仍需登录。
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                // 排除公开路径
                .addExclude(
                        "/auth/login",
                        "/auth/register",
                        "/login",
                        "/register",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/error"
                )
                // 认证逻辑
                .setAuth(r -> {
                    // 公开的 GET 接口：放行
                    SaRouter.match("/articles", "/articles/*", "/statistics",
                                    "/categories", "/categories/*", "/tags", "/tags/*",
                                    "/comments/article/*", "/likes/*", "/likes/*/status",
                                    "/favorites/*", "/favorites/*/status")
                            .matchMethod("GET")
                            .stop();

                    // 其他所有接口：需要登录
                    StpUtil.checkLogin();
                })
                // 认证失败：写 JSON 响应并标记已提交，阻止请求继续到达 Controller
                .setError(e -> {
                    int code = 401;
                    String message = "未登录或登录已过期";
                    if (e instanceof NotLoginException nle) {
                        if (NotLoginException.NOT_TOKEN.equals(nle.getType())) {
                            message = "未能读取到有效token";
                        } else if (NotLoginException.INVALID_TOKEN.equals(nle.getType())) {
                            code = 4011;
                            message = "Token无效";
                        } else if (NotLoginException.TOKEN_TIMEOUT.equals(nle.getType())) {
                            code = 4012;
                            message = "Token已过期";
                        }
                    }
                    writeJsonResponse(code, message);
                });
    }

    /**
     * 通过 SaHolder 获取 HttpServletResponse 并写入 JSON 响应
     */
    private void writeJsonResponse(int code, String message) {
        Object source = SaHolder.getResponse().getSource();
        if (source instanceof HttpServletResponse response) {
            try {
                if (!response.isCommitted()) {
                    response.setStatus(code);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    String json = "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}";
                    PrintWriter writer = response.getWriter();
                    writer.print(json);
                    writer.flush();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
