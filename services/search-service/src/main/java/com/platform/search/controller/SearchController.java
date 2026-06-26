package com.platform.search.controller;

import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.Result;
import com.platform.search.domain.dto.ArticleSearchDTO;
import com.platform.search.domain.vo.ArticleSearchVO;
import com.platform.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@Tag(name = "搜索管理", description = "文章搜索相关接口")
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 搜索文章（全文搜索 + 多维筛选）
     */
    @Operation(summary = "搜索文章", description = "根据关键词、分类、标签、作者、日期范围等条件搜索文章")
    @PostMapping("/articles")
    public Result<PageResult<ArticleSearchVO>> searchArticles(@RequestBody ArticleSearchDTO searchDTO) {
        return Result.success(searchService.searchArticles(searchDTO));
    }

    /**
     * 语义搜索（向量相似度 + 多维筛选）
     */
    @Operation(summary = "语义搜索", description = "使用向量相似度搜索，支持作者、日期范围、标签筛选")
    @PostMapping("/articles/semantic")
    public Result<PageResult<ArticleSearchVO>> semanticSearch(@RequestBody ArticleSearchDTO searchDTO) {
        return Result.success(searchService.semanticSearch(searchDTO));
    }

    /**
     * 获取搜索建议
     */
    @Operation(summary = "获取搜索建议", description = "根据关键词获取搜索建议")
    @GetMapping("/suggestions")
    public Result<List<String>> getSuggestions(
            @Parameter(description = "关键词", required = true) @RequestParam String keyword) {
        return Result.success(searchService.getSuggestions(keyword));
    }
}
