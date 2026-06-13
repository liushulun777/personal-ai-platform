package com.platform.blog.service;

import com.platform.blog.domain.vo.BlogStatisticsVO;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取博客统计数据
     */
    BlogStatisticsVO getBlogStatistics();
}
