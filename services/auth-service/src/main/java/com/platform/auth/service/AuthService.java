package com.platform.auth.service;

import com.platform.auth.domain.dto.LoginDTO;
import com.platform.auth.domain.dto.RegisterDTO;
import com.platform.auth.domain.vo.LoginVO;
import com.platform.auth.domain.vo.UserInfoVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return 登录响应（含token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求
     */
    void register(RegisterDTO registerDTO);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getUserInfo();
}
