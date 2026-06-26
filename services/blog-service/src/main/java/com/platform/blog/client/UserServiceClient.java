package com.platform.blog.client;

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
}
