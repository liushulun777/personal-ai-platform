package com.platform.blog.domain.dto;

import lombok.Data;

/**
 * 用户信息 DTO
 */
@Data
public class UserDTO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;
}
