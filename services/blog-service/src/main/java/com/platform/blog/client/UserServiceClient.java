package com.platform.blog.client;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.platform.blog.domain.dto.UserDTO;
import com.platform.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 用户服务客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;


    /**
     * 获取用户信息（返回强类型 DTO）
     */
    @SentinelResource(
        value = "getUserDTOById",
        blockHandler = "getUserDTOByIdBlockHandler",
        fallback = "getUserDTOByIdFallback"
    )
    public UserDTO getUserDTOById(Long userId) {
        try {
            String url = "http://system-service/users/" + userId;
            ResponseEntity<Result<UserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Result<UserDTO> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                return result.getData();
            }
            return null;
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 限流/降级处理方法（BlockException）
     */
    public UserDTO getUserDTOByIdBlockHandler(Long userId, BlockException ex) {
        log.warn("用户服务被限流或降级，userId: {}, 原因: {}", userId, ex.getClass().getSimpleName());
        UserDTO fallback = new UserDTO();
        fallback.setId(userId);
        fallback.setNickname("用户信息获取失败");
        return fallback;
    }

    /**
     * 业务异常处理方法（Throwable）
     */
    public UserDTO getUserDTOByIdFallback(Long userId, Throwable t) {
        log.error("用户服务调用失败，userId: {}, 原因: {}", userId, t.getMessage());
        UserDTO fallback = new UserDTO();
        fallback.setId(userId);
        fallback.setNickname("用户信息获取失败");
        return fallback;
    }
}
