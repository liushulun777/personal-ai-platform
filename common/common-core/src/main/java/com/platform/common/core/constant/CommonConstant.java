package com.platform.common.core.constant;

/**
 * 通用常量
 */
public final class CommonConstant {

    private CommonConstant() {}

    /**
     * 逻辑删除：未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 逻辑删除：已删除
     */
    public static final Integer DELETED = 1;

    /**
     * 状态：启用
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 状态：禁用
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * 请求头：Token
     */
    public static final String HEADER_TOKEN = "Authorization";

    /**
     * 请求头：用户ID
     */
    public static final String HEADER_USER_ID = "X-User-Id";

    /**
     * 请求头：用户名
     */
    public static final String HEADER_USERNAME = "X-Username";

    /**
     * 超级管理员角色标识
     */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * 超级管理员用户ID
     */
    public static final Long SUPER_ADMIN_ID = 1L;

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;
}
