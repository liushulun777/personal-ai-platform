package com.platform.blog.controller;

import com.platform.blog.domain.vo.BlogStatisticsVO;
import com.platform.blog.service.StatisticsService;
import com.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计控制器
 */
@Tag(name = "统计管理", description = "博客统计数据接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取博客统计数据")
    @GetMapping
    public Result<BlogStatisticsVO> getBlogStatistics() {
        BlogStatisticsVO statistics = statisticsService.getBlogStatistics();
        return Result.success(statistics);
    }
}
